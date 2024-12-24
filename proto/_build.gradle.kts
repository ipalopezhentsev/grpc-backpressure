plugins {
    id("grpc.backpressure.java-library-conventions")
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:3.6.1")
  implementation("io.grpc:grpc-stub:1.15.1")
  implementation("io.grpc:grpc-protobuf:1.15.1")

  // Extra proto source files besides the ones residing under
  // "src/main".
  protobuf(files("lib/protos.tar.gz"))
  protobuf(files("ext/"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.plugins {
                id("grpc") { }
            }
        }
    }
}


// protobuf {
//   protoc {
//     // The artifact spec for the Protobuf Compiler
//     artifact = "com.google.protobuf:protoc:3.6.1"
//   }
//   plugins {
//     // Optional: an artifact spec for a protoc plugin, with "grpc" as
//     // the identifier, which can be referred to in the "plugins"
//     // container of the "generateProtoTasks" closure.
//     id("grpc") {
//       artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
//     }
//   }
//   generateProtoTasks {
//     ofSourceSet("main").forEach {
//       it.plugins {
//         // Apply the "grpc" plugin whose spec is defined above, without
//         // options. Note the braces cannot be omitted, otherwise the
//         // plugin will not be added. This is because of the implicit way
//         // NamedDomainObjectContainer binds the methods.
//         id("grpc") { }
//       }
//     }
//   }
