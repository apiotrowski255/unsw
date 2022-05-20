package org.ea;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jdk.httpserver.HttpServerImpl;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpServerExample {

   public static void main(String[] args) {
      try {
         HttpServer httpServer = HttpServerImpl.create(new InetSocketAddress(9000), 0);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
