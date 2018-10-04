/*
 * Copyright (C) 2018  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
use failure::Error;
use fallible_iterator::FallibleIterator;
use postgres::{Connection,TlsMode};
use std::collections::HashSet;
use std::path::PathBuf;
use std::sync::mpsc::{channel,Sender};
use std::thread;
use std::time::{Duration,Instant};
use mirror;
use resource::{lookup_resource,ALL};

static OUTPUT_DIR: &str = "/var/www/html/iris/";

pub fn start(username: String, host: Option<String>) -> Result<(), Error> {
    // Format path for unix domain socket
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    let (tx, rx) = channel();
    let db = thread::spawn(move || {
        db_thread(uds, tx).unwrap();
    });
    mirror::start(host, &username, rx);
    db.join().expect("db_thread panicked!");
    Ok(())
}

fn db_thread(uds: String, tx: Sender<PathBuf>) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    // The postgresql crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).
    conn.execute("SET TIME ZONE 'US/Central'", &[])?;
    conn.execute("LISTEN tms", &[])?;
    // Initialize all the resources
    for r in ALL.iter() {
        fetch_resource_timed(&conn, &tx, r)?;
    }
    notify_loop(&conn, tx)
}

fn fetch_resource_timed(conn: &Connection, tx: &Sender<PathBuf>, n: &str)
    -> Result<(), Error>
{
    let t = Instant::now();
    if let Some(c) = fetch_resource(&conn, tx, &n)? {
        println!("{}: wrote {} rows in {:?}", &n, c, t.elapsed());
    } else {
        println!("{}: unknown resource", &n);
    }
    Ok(())
}

fn fetch_resource(conn: &Connection, tx: &Sender<PathBuf>, n: &str)
    -> Result<Option<u32>, Error>
{
    if let Some(r) = lookup_resource(n) {
        Ok(Some(r.fetch(&conn, OUTPUT_DIR, tx)?))
    } else {
        Ok(None)
    }
}

fn notify_loop(conn: &Connection, tx: Sender<PathBuf>) -> Result<(), Error> {
    let nots = conn.notifications();
    let mut ns = HashSet::new();
    loop {
        for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
            let n = n?;
            ns.insert(n.payload);
        }
        for n in ns.drain() {
            fetch_resource_timed(&conn, &tx, &n)?;
        }
    }
}
