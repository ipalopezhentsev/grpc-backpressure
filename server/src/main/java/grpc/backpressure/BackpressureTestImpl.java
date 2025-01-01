package grpc.backpressure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import grpc.backpressure.proto.BackpressureTestGrpc;
import grpc.backpressure.proto.Reply;
import grpc.backpressure.proto.Request;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

public class BackpressureTestImpl extends BackpressureTestGrpc.BackpressureTestImplBase {
    private static final Logger log = LoggerFactory.getLogger(BackpressureTestImpl.class);

    @Override
    public void infiniteStream(Request request, StreamObserver<Reply> responseObserver) {
        var srvResponseObserver = (ServerCallStreamObserver<Reply>) responseObserver;
        // this is needed only for flow control of client streams:
        // srvResponseObserver.disableAutoRequest();
        int i = 1;
        try {
            while (!srvResponseObserver.isCancelled()) {
                if (srvResponseObserver.isReady()) {
                    var repl = Reply.newBuilder().setMessage(Integer.toString(i)).build();
                    // if we don't check for .isReady() and have a slow client, it will cause this
                    // server to go out of memory soon.
                    responseObserver.onNext(repl);
                    log.info("i={}", i);
                    i++;
                } else {
                    log.info("Client not ready");
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        log.error("", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            responseObserver.onCompleted();
        } catch (Throwable ex) {
            responseObserver.onError(ex);
        }
    }
}