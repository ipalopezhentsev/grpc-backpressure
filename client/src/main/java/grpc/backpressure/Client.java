package grpc.backpressure;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import grpc.backpressure.proto.BackpressureTestGrpc;
import grpc.backpressure.proto.BackpressureTestGrpc.BackpressureTestBlockingStub;
import grpc.backpressure.proto.BackpressureTestGrpc.BackpressureTestStub;
import grpc.backpressure.proto.Reply;
import grpc.backpressure.proto.Request;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final ManagedChannel channel;
    private final BackpressureTestBlockingStub blockingStub;
    private final BackpressureTestStub asyncStub;

    public Client(String server) {
        channel = Grpc.newChannelBuilder(server + ":50051", InsecureChannelCredentials.create()).build();
        var svcDescr = BackpressureTestGrpc.getServiceDescriptor();
        log.info(svcDescr.toString());
        blockingStub = BackpressureTestGrpc.newBlockingStub(channel);
        asyncStub = BackpressureTestGrpc.newStub(channel);
    }

    public void testViaBlockingStub() {
        var cnt = new AtomicInteger();
        try {
            blockingStub.infiniteStream(Request.getDefaultInstance()).forEachRemaining(resp -> {
                var i = cnt.incrementAndGet();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.error("", e);
                }
                log.info("i={}, resp={}", i, resp);
            });
        } catch (Exception ex) {
            log.error("Server stopped replying, last count={}", cnt.get(), ex);
        }
    }

    public void testViaAsyncStub() throws InterruptedException {
        var cnt = new AtomicInteger();
        var latch = new CountDownLatch(1);
        //in principle, we need ClientResponseObserver only for getting ClientCallStreamObserver for cancelling.
        //we do not need explicit disableAutoRequestWithInitial(1)/requestStream.request(1) backpressure calls
        //because that's what default gRPC implementation does anyway.
        final var observer = new ClientResponseObserver<Request, Reply>() {
            private ClientCallStreamObserver<Request> requestStream = null;

            @Override
            public void beforeStart(ClientCallStreamObserver<Request> requestStream) {
                this.requestStream = requestStream;
                // this.requestStream.disableAutoRequestWithInitial(1);
            }

            @Override
            public void onNext(Reply value) {
                var i = cnt.incrementAndGet();
                if (i == 100_000) {
                    requestStream.cancel("dont wanna anymore", null);
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.error("", e);
                }
                log.info("i={}, resp={}", i, value);
                //default impl will do this:
                // this.requestStream.request(1);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Server stopped replying, last count={}", cnt.get(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Done");
                latch.countDown();
            }
        };
        asyncStub.infiniteStream(Request.getDefaultInstance(), observer);
        latch.await();
    }

    public static void main(String[] args) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        var maxMemory = runtime.maxMemory();
        var cpus = runtime.availableProcessors();
        var poolThrNum = ForkJoinPool.getCommonPoolParallelism();
        log.info("Client; max mem available: {} MB; cpus: {}; pool thr: {}",
                maxMemory / 1024 / 1024, cpus, poolThrNum);
        var server = "localhost";
        var envServer = System.getenv("SERVER_NAME");
        if (envServer != null) {
            server = envServer;
        }
        var clnt = new Client(server);
        // both blocking & async calls work correctly from the standpoint of not causing
        // out of memory in client & server
        // clnt.testViaBlockingStub();
        clnt.testViaAsyncStub();
        log.info("done");
    }
}
