import com.google.protobuf.gradle.*

plugins {
    id("grpc.backpressure.java-library-conventions")
    id("idea")
    id("com.google.protobuf") version "0.9.4"
}

val protoVersion = "3.25.5"
val grpcVersion = "1.69.0"

dependencies {
    api("io.grpc:grpc-protobuf:${grpcVersion}")
    api("io.grpc:grpc-stub:${grpcVersion}")
    implementation("io.grpc:grpc-services:${grpcVersion}")
    implementation("com.google.protobuf:protobuf-java:${protoVersion}")
    runtimeOnly("io.grpc:grpc-netty-shaded:${grpcVersion}")
    compileOnly("javax.annotation:javax.annotation-api:1.3.1")
}

protobuf {
  protoc {
    // The artifact spec for the Protobuf Compiler
    artifact = "com.google.protobuf:protoc:${protoVersion}"
  }
  plugins {
    // Optional: an artifact spec for a protoc plugin, with "grpc" as
    // the identifier, which can be referred to in the "plugins"
    // container of the "generateProtoTasks" closure.
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.plugins {
        // Apply the "grpc" plugin whose spec is defined above, without
        // options. Note the braces cannot be omitted, otherwise the
        // plugin will not be added. This is because of the implicit way
        // NamedDomainObjectContainer binds the methods.
        id("grpc") { }
      }
    }
  }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}
