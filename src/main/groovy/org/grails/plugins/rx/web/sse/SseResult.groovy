package org.grails.plugins.rx.web.sse

import groovy.transform.CompileStatic

/**
 * A Server Sent Events event that can be written to an event stream
 */
@CompileStatic
class SseResult implements Writable {

    static final String ID_PREFIX = 'id'
    static final String EVENT_PREFIX = 'event'
    static final String DATA_PREFIX = 'data'
    static final String RETRY_PREFIX = 'retry'

    String id = null
    String event = null
    String comment = null
    Long retry = null
    Writable data = null

    @Override
    Writer writeTo(Writer out) throws IOException {
        if ((!id && !event && !comment && !data)) {
            return out
        }

        if (comment != null) out.write(": ${toSseString(comment)}\n")
        if (id != null) out.write("$ID_PREFIX: ${toSseString(id)}\n")
        if (event != null) out.write("$EVENT_PREFIX: ${toSseString(event)}\n")
        if (retry != null) out.write("$RETRY_PREFIX: $retry\n")
        if (data != null) {
            out.write("$DATA_PREFIX: ")
            data.writeTo(new SseWriter(out, DATA_PREFIX))
            out.write('\n')
        }
        out.write('\n')
        return out
    }

    static String toSseString(String input) {
        input.replace('\n', ' ')
    }
}