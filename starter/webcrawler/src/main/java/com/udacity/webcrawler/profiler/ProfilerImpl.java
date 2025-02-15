package com.udacity.webcrawler.profiler;

import java.nio.file.Files;
import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
    ProfilingMethodInterceptor methodInterceptor=new ProfilingMethodInterceptor(delegate,clock,state,startTime);
    Object dynamicProxy;
    Method [] methods=klass.getDeclaredMethods();
    if(methods.length == 0){
      //No methods found...Empty Interface
      throw new IllegalArgumentException();
    }
    try {
      dynamicProxy= Proxy.newProxyInstance(
              delegate.getClass().getClassLoader(),
              new Class<?>[]{klass},
              methodInterceptor);
    }catch (IllegalArgumentException e){
      throw e;
    }
    return (T) dynamicProxy;
  }

  @Override
  public void writeData(Path path) {
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    File file=new File(path.toString());
    if(file.isFile()){
      //file exsists...append the contents to this file
      try(
              Writer writer= new BufferedWriter(new FileWriter(file,true));
      ){
        writeData(writer);
      }catch (IOException ioException){
        ioException.printStackTrace();
      }

    }
    else{
      //create a file and write
      try(
              Writer writer= Files.newBufferedWriter(file.toPath());
      ){
        writeData(writer);
      }catch (IOException ioException){
        ioException.printStackTrace();
      }
    }


  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
