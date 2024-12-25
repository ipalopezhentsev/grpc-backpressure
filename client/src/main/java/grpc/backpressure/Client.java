/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package grpc.backpressure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import grpc.backpressure.proto.BackpressureTestGrpc;
import grpc.backpressure.proto.Request;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        log.info("Client");
        String target = "server:50051";
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        var blockingStub = BackpressureTestGrpc.newBlockingStub(channel);
        blockingStub.infiniteStream(Request.getDefaultInstance()).forEachRemaining(resp-> {
            log.info(resp.toString());
        });
    }
}