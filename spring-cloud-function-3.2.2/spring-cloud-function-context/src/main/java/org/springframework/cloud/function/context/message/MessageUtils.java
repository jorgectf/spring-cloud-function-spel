/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.context.message;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.cloud.function.core.FluxWrapper;
import org.springframework.cloud.function.core.Isolated;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;


/**
 * @author Dave Syer
 * @author Oleg Zhurakousky
 */
public abstract class MessageUtils {

	/**
	 * Value for 'message-type' typically use as header key.
	 */
	public static String MESSAGE_TYPE = "message-type";

	/**
	 * Value for 'target-protocol' typically use as header key.
	 */
	public static String TARGET_PROTOCOL = "target-protocol";

	/**
	 * Value for 'target-protocol' typically use as header key.
	 */
	public static String SOURCE_TYPE = "source-type";

	/**
<<<<<<< HEAD
	 * Create a message for the handler. If the handler is a wrapper for a function in an
	 * isolated class loader, then the message will be created with the target class
	 * loader (therefore the {@link Message} class must be on the classpath of the target
	 * class loader).
	 * @param handler the function that will be applied to the message
	 * @param payload the payload of the message
	 * @param headers the headers for the message
	 * @return a message with the correct class loader
	 */
	public static Object create(Object handler, Object payload,
			Map<String, Object> headers) {
		if (handler instanceof FluxWrapper) {
			handler = ((FluxWrapper<?>) handler).getTarget();
		}
		if (payload instanceof Message) {
			headers = new HashMap<>(headers);
			headers.putAll(((Message<?>) payload).getHeaders());
			payload = ((Message<?>) payload).getPayload();
		}
		if (!(handler instanceof Isolated)) {
			return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
		}
		ClassLoader classLoader = ((Isolated) handler).getClassLoader();
		Class<?> builder = ClassUtils.resolveClassName(MessageBuilder.class.getName(),
				classLoader);
		Method withPayload = ClassUtils.getMethod(builder, "withPayload", Object.class);
		Method copyHeaders = ClassUtils.getMethod(builder, "copyHeaders", Map.class);
		Method build = ClassUtils.getMethod(builder, "build");
		Object instance = ReflectionUtils.invokeMethod(withPayload, null, payload);
		ReflectionUtils.invokeMethod(copyHeaders, instance, headers);
		return ReflectionUtils.invokeMethod(build, instance);
	}

	/**
	 * Convert a message from the handler into one that is safe to consume in the caller's
	 * class loader. If the handler is a wrapper for a function in an isolated class
	 * loader, then the message will be created with the target class loader (therefore
	 * the {@link Message} class must be on the classpath of the target class loader).
	 * @param handler the function that generated the message
	 * @param message the message to convert
	 * @return a message with the correct class loader
	 */
	public static Message<?> unpack(Object handler, Object message) {
		if (handler instanceof FluxWrapper) {
			handler = ((FluxWrapper<?>) handler).getTarget();
		}
		if (!(handler instanceof Isolated)) {
			if (message instanceof Message) {
				return (Message<?>) message;
			}
			return MessageBuilder.withPayload(message).build();
		}
		ClassLoader classLoader = ((Isolated) handler).getClassLoader();
		Class<?> type = ClassUtils.isPresent(Message.class.getName(), classLoader)
				? ClassUtils.resolveClassName(Message.class.getName(), classLoader)
				: null;
		Object payload;
		Map<String, Object> headers;
		if (type != null && type.isAssignableFrom(message.getClass())) {
			Method getPayload = ClassUtils.getMethod(type, "getPayload");
			Method getHeaders = ClassUtils.getMethod(type, "getHeaders");
			payload = ReflectionUtils.invokeMethod(getPayload, message);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) ReflectionUtils
					.invokeMethod(getHeaders, message);
			headers = map;
		}
		else {
			payload = message;
			headers = Collections.emptyMap();
		}
		return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
	}

	/**
	 * Returns (payload, headers) structure identical to `message` while
	 * substituting headers with case insensitive map.
	 */
	public static MessageStructureWithCaseInsensitiveHeaderKeys toCaseInsensitiveHeadersStructure(Message<?> message) {
		return new MessageStructureWithCaseInsensitiveHeaderKeys(message);
	}

	/**
	 * !!! INTERNAL USE ONLY, MAY CHANGE OR REMOVED WITHOUT NOTICE!!!
	 */
	@SuppressWarnings({"rawtypes"})
	public static class MessageStructureWithCaseInsensitiveHeaderKeys {
		private final Object payload;
		private final Map headers;

		@SuppressWarnings("unchecked")
		MessageStructureWithCaseInsensitiveHeaderKeys(Message message) {
			this.payload = message.getPayload();
			this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.headers.putAll(message.getHeaders());
		}
		public Object getPayload() {
			return payload;
		}

		public Map getHeaders() {
			return headers;
		}
	}
}
