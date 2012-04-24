package org.vaadin.aceeditor.gwt.shared;

public class Util {

	// TODO: A better way to convert would be better. This is a bit inefficient.
	public static int cursorPosFromLineCol(String text, int line, int col,
			int firstLineNum) {
		return cursorPosFromLineCol(text.split("\n", -1), line, col,
				firstLineNum);
	}

	public static int cursorPosFromLineCol(String[] lines, int line, int col,
			int firstLineNum) {
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

	// TODO: A better way to convert would be better. This is a bit inefficient.
	public static int[] lineColFromCursorPos(String text, int pos,
			int firstLineNum) {
		return lineColFromCursorPos(text.split("\n", -1), pos, firstLineNum);
	}

	public static int[] lineColFromCursorPos(String[] lines, int pos,
			int firstLineNum) {
		int lineno = 0;
		int col = pos;
		for (String li : lines) {
			if (col <= li.length()) {
				break;
			}
			lineno += 1;
			col -= (li.length() + 1);
		}
		lineno += firstLineNum;

		return new int[] { lineno, col };
	}

	public static int count(char c, String text) {
		int n = 0;
		int from = 0;
		while (true) {
			int index = text.indexOf(c, from);
			if (index == -1) {
				return n;
			} else {
				++n;
				from = index + 1;
			}
		}
	}

	public static int startColOfCursorLine(String text, int cursor) {
		int i = cursor;
		while (i > 0) {
			char c = text.charAt(i - 1);
			if (c == '\n') {
				break;
			}
			--i;
		}
		return i;
	}

	public static String indentationStringOfCursorLine(String text, int cursor) {
		int lineStart = startColOfCursorLine(text, cursor);
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
}
