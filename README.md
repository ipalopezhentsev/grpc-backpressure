Create artificially slow client reading fast server stream.
The idea is to check how gRPC flow control allows to solve this mismatch.

The proper (desired) functioning consists of two parts:
1) client (and its memory buffers) is not overwhelmed by incoming server messages
2) server should slow down, and not go out of memory buffering unsent client messages
i.e. infinite server stream should be stable even on small heaps and slow clients.

gRPC docs seem to imply that both 1) and 2) work automagically:
https://grpc.io/docs/guides/flow-control/
However, simple test shows that no, it's only 1) that works, 2) does NOT work without
special treatment at server, i.e. it must be checking onReady and throttle itself.

It's not my lack of understanding, gRPC authors write themselves that server's onNext() 
was never meant to block in case of client's backpressure:
https://github.com/grpc/grpc-java/issues/1549#issuecomment-218549282
