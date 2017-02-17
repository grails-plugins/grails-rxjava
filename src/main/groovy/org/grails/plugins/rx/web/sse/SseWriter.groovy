package org.grails.plugins.rx.web.sse

import groovy.transform.CompileStatic

@CompileStatic
class SseWriter extends FilterWriter {

    private static final char NEW_LINE = '\n'
    private final String prefix

    SseWriter(Writer out, String prefix) {
        super(out)
        this.prefix = prefix + ': '
    }

    void write(int c) throws IOException {
        if (c == (int) NEW_LINE) {
            out.write(c)
            out.write(prefix)
        } else {
            out.write(c)
        }
    }

    void write(char[] cbuf, int off, int len) throws IOException {
        int lastNewLine = 0

        for (int i = 0; i < len; ++i) {
            if (cbuf[off + i] == NEW_LINE) {
                out.write(cbuf, off + lastNewLine, i + 1 - lastNewLine)
                out.write(prefix)
                lastNewLine = i + 1
            }
        }
        out.write(cbuf, off + lastNewLine, len - lastNewLine)
    }

    void write(String str, int off, int len) throws IOException {
        int lastNewLine = 0

        for (int i = 0; i < len; ++i) {
            if (str.charAt(off + i) == NEW_LINE) {
                out.write(str, off + lastNewLine, i + 1 - lastNewLine)
                out.write(prefix)
                lastNewLine = i + 1
            }
        }
        out.write(str, off + lastNewLine, len - lastNewLine)
    }

    @Override
    void close() throws IOException {
        // don't close underlying stream
    }
}