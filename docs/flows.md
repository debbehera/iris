# Flows

A **flow** is a video stream which is not provided directly by a [camera], but
by a [streambed] server.  Flows can be used to transcode from one video encoding
to another, overlay text, or rebroadcast a unicast RTSP stream to a [multicast]
address.

Streambed can run on one or more dedicated computers, and is controlled by IRIS
through the `streambed` [protocol].  A [controller] and associated [comm link]
must exist for each streambed server.  Each flow must be assigned to an [IO pin]
on a streambed controller.

## Configuration

Select `View ➔ Video ➔ Flows` menu item

To configure a flow, edit the first eight fields in the table.  A flow can be
either a _camera flow_ or _video monitor flow_, but not both.

Field            | Description
-----------------|----------------------------------------------------------
Flow             | Flow name
Restricted       | Flag restricting flow to only published cameras
Location overlay | Flag indicating whether [camera] location should be added
Quality          | Encoder [stream] quality
Camera           | [Camera] name
Monitor num      | [Video monitor] number
Address          | _Sink_ address
Port             | _Sink_ port

## Status

The current flow status is displayed in the last 4 fields.

Field  | Description
-------|--------------------------------
State  | `STARTING`, `PLAYING`, `FAILED`
Pushed | Pushed packet count
Lost   | Lost packet count
Late   | Late packet count

## Camera Flows

A _camera flow_ uses a [camera] stream for its _source_.  The `camera` field
should be configured, but `monitor num` must be blank.

The camera's [encoder type] must contain a [stream] with the same `quality`
value as the flow, but with `flow` unchecked.  That stream defines the _source_
of the flow.

If `address` and `port` are specified, they define the flow's _sink_.
Otherwise, it is defined by a stream of the camera's encoder type with `flow`
checked and a matching `quality` value.

## Video Monitor Flows

A _video monitor flow_ uses a [video monitor] for its _source_ — more precisely,
the [camera] currently displayed on that monitor.  The `monitor num` field
should be configured, but `camera` must be blank.

The _source_ is defined by the current camera displayed on the specified monitor
number.  That camera's [encoder type] must contain a [stream] with the same
`quality` value as the flow.  If multiple streams match, the one with `flow`
checked is used.

The `address` and `port` fields define the flow's _sink_.

## Transcoding

If the _sink_ encoding is different than the _source_, the flow will be
_transcoded_ by streambed.  Warning: transcoding requires more CPU time than
simply rebroadcasting.


[camera]: cameras.html
[comm link]: comm_links.html
[controller]: controllers.html
[encoder type]: cameras.html#encoder-types
[IO pin]: controllers.html#io-pins
[multicast]: https://en.wikipedia.org/wiki/Multicast_address
[protocol]: comm_links.html#protocols
[stream]: cameras.html#streams
[streambed]: https://github.com/mnit-rtmc/streambed
[video monitor]: video.html