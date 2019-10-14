package org.vaadin.aceeditor.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Util {

	// TODO: A better way to convert would be better. This is a bit inefficient.
	public static int cursorPosFromLineCol(final String text, final int line, final int col,
			final int firstLineNum) {
		return Util.cursorPosFromLineCol(text.split("\n", -1), line, col,
				firstLineNum);
	}

	public static int cursorPosFromLineCol(final String[] lines, int line, final int col,
			final int firstLineNum) {
		line -= firstLineNum;
		int pos = 0;
		for (int currLine = 0; currLine < lines.length; ++currLine) {
			if (currLine < line) {
				pos += lines[currLine].length() + 1;
			} else if (currLine == line) {
				pos += col;
				break;
			}
		}
		return pos;
	}

	public static int cursorPosFromLineCol(final int[] lineLengths, int line, final int col,
			final int firstLineNum) {
		line -= firstLineNum;
		int pos = 0;
		for (int currLine = 0; currLine < lineLengths.length; ++currLine) {
			if (currLine < line) {
				pos += lineLengths[currLine] + 1;
			} else if (currLine == line) {
				pos += col;
				break;
			}
		}
		return pos;
	}

	// TODO: A better way to convert would be better. This is a bit inefficient.
	public static int[] lineColFromCursorPos(final String text, final int pos,
			final int firstLineNum) {
		return Util.lineColFromCursorPos(text.split("\n", -1), pos, firstLineNum);
	}

	public static int[] lineColFromCursorPos(final String[] lines, final int pos,
			final int firstLineNum) {
		int lineno = 0;
		int col = pos;
		for (final String li : lines) {
			if (col <= li.length()) {
				break;
			}
			lineno += 1;
			col -= (li.length() + 1);
		}
		lineno += firstLineNum;

		return new int[] { lineno, col };
	}

	public static int[] lineColFromCursorPos(final int[] lineLengths, final int pos,
			final int firstLineNum) {
		int lineno = 0;
		int col = pos;
		for (final int len : lineLengths) {
			if (col <= len) {
				break;
			}
			lineno += 1;
			col -= (len + 1);
		}
		lineno += firstLineNum;

		return new int[] { lineno, col };
	}

	public static int count(final char c, final String text) {
		int n = 0;
		int from = 0;
		while (true) {
			final int index = text.indexOf(c, from);
			if (index == -1) {
				return n;
			} else {
				++n;
				from = index + 1;
			}
		}
	}

	public static int startColOfCursorLine(final String text, final int cursor) {
		int i = cursor;
		while (i > 0) {
			final char c = text.charAt(i - 1);
			if (c == '\n') {
				break;
			}
			--i;
		}
		return i;
	}

	public static String indentationStringOfCursorLine(final String text, final int cursor) {
		final int lineStart = Util.startColOfCursorLine(text, cursor);
		int firstCharAt = lineStart;
		while (firstCharAt < text.length()) {
			// TODO: tab indentation?
			if (text.charAt(firstCharAt) != ' ') {
				break;
			}
			++firstCharAt;
		}
		return text.substring(lineStart, firstCharAt);
	}

	// There's no string join method in java libs??
	public static String join(final String[] lines) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final String li : lines) {
			if (first) {
				sb.append(li);
				first = false;
			} else {
				sb.append("\n").append(li);
			}

		}
		return sb.toString();
	}

	public static <K,V> boolean sameMaps(
			final Map<K, V> map1,
			final Map<K, V> map2) {
		if (map1.size() != map2.size()) {
			return false;
		}
		for(final Entry<K, V> e : map1.entrySet()) {
			final V m1 = e.getValue();
			final V m2 = map2.get(e.getKey());
			if (!m1.equals(m2)) {
				return false;
			}
		}
		return true;
	}

	public static <V> boolean sameSets(final Set<V> set1, final Set<V> set2) {
		if (set1.size()!=set2.size()) {
			return false;
		}
		for (final V v : set1) {
			if (!set2.contains(v)) {
				return false;
			}
		}
		return true;
	}

	public static String replaceContents(final AceRange ofThis, final String inText, final String withThis) {
		final String[] lines = inText.split("\n", -1);
		final int start = Util.cursorPosFromLineCol(lines, ofThis.getStartRow(), ofThis.getStartCol(), 0);
		final int end = Util.cursorPosFromLineCol(lines, ofThis.getEndRow(), ofThis.getEndCol(), 0);
		return inText.substring(0, start) + withThis + inText.substring(end);
	}

}
