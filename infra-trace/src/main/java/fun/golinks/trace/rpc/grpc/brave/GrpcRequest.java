/*
 * Copyright The OpenZipkin Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package fun.golinks.trace.rpc.grpc.brave;

import brave.rpc.RpcTracing;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Allows access gRPC specific aspects of a client or server request during sampling and parsing.
 *
 * <p>
 * Here's an example that adds default tags, and if gRPC, the {@linkplain MethodDescriptor#getType() method type}:
 *
 * @see GrpcResponse
 * @see GrpcClientRequest
 * @see GrpcServerRequest
 * @see RpcTracing#clientRequestParser()
 * @see RpcTracing#serverRequestParser()
 * 
 * @since 5.12
 */
// NOTE: gRPC is Java 1.7+, so we cannot add methods to this later
public interface GrpcRequest {
    // method would be a nicer name, but this is used in instanceof with an RpcRequest
    // and RpcRequest.method() has a String result
    MethodDescriptor<?, ?> methodDescriptor();

    Metadata headers();
}
