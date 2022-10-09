package com.udacity.webcrawler.profiler;

import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final ProfilingState state ;
  private final ZonedDateTime startTime;
  private Object delegate;

  // TODO: You will need to add more instance fields and constructor arguments to this class.

  public <T> ProfilingMethodInterceptor( T delegate,Clock clock,ProfilingState state,ZonedDateTime startTime) {
    this.clock=clock;
    this.state=state;
    this.startTime=startTime;
    this.delegate=delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws  Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    //Object object = null;
    Instant startingInst;
      if (method.isAnnotationPresent(Profiled.class)) {

        startingInst= clock.instant();
        try {
          return method.invoke(delegate, args);
          //return object;
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        } catch (Exception e) {
          throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
        } finally {
          Instant endingInst = clock.instant();
          Duration duration = Duration.between(startingInst, endingInst);
          state.record(delegate.getClass(), method, duration);

        }

    }
    //Invoke methods not annotated with profiled
    try {
        return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
        throw e.getTargetException();
    } catch (Exception e) {
        throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
    }
  }

}
