package grpc.backpressure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import grpc.backpressure.proto.BackpressureTestGrpc;
import grpc.backpressure.proto.Reply;
import grpc.backpressure.proto.Request;
import io.grpc.stub.StreamObserver;

public class BackpressureTestImpl extends BackpressureTestGrpc.BackpressureTestImplBase {
    private static final Logger log = LoggerFactory.getLogger(BackpressureTestImpl.class);

    @Override
    public void infiniteStream(Request request, StreamObserver<Reply> responseObserver) {
        int i = 1;
        // try {
            while (true) {
                var repl = Reply.newBuilder().setMessage(Integer.toString(i)).build();
                //slow client without backpressure will cause this server to go out of memory soon.
                responseObserver.onNext(repl);
                if (i % 50_000 == 0) {
                    log.info("i={}", i);
                }
                i++;
            }
            // responseObserver.onCompleted();
        // } catch (Throwable ex) {
        //     System.err.println("Exiting due to resources, reached count=" + i);
        //     ex.printStackTrace();
        //     throw ex;
        // }
    }
}