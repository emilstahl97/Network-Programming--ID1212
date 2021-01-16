package com.kth.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/***
 * Interface for games that read and write across streams.
 *
 */

public interface Servable {

	void serve(BufferedReader input, PrintWriter output) throws IOException;
}
