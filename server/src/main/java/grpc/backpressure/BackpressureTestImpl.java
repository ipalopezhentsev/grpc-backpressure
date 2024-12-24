package grpc.backpressure;

import grpc.backpressure.proto.BackpressureTestGrpc;
import grpc.backpressure.proto.Reply;
import grpc.backpressure.proto.Request;
import io.grpc.stub.StreamObserver;

public class BackpressureTestImpl extends BackpressureTestGrpc.BackpressureTestImplBase {

    @Override
    public void infiniteStream(Request request, StreamObserver<Reply> responseObserver) {
        var repl = Reply.newBuilder().setMessage("Hello!").build();
        responseObserver.onNext(repl);
        responseObserver.onCompleted();
    }
}