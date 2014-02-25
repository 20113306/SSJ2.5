

/*
 * Class:        CIRProcessEuler
 * Description:  
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Université de Montréal
 * Organization: DIRO, Université de Montréal
 * @author       
 * @since

 * SSJ is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or
 * any later version.

 * SSJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * A copy of the GNU General Public License is available at
   <a href="http://www.gnu.org/licenses">GPL licence site</a>.
 */

package umontreal.iro.lecuyer.stochprocess;
import umontreal.iro.lecuyer.rng.*;
import umontreal.iro.lecuyer.probdist.*;
import umontreal.iro.lecuyer.randvar.*;



/**
 * .
 * 
 * This class represents a <SPAN  CLASS="textit">CIR</SPAN> process
 * as in {@link CIRProcess}, but
 * the process is generated using the simple Euler scheme
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay"><A NAME="eq:cir-seq-euler"></A>
 * <I>X</I>(<I>t</I><SUB>j</SUB>) - <I>X</I>(<I>t</I><SUB>j-1</SUB>) = <I>&#945;</I>(<I>b</I> - <I>X</I>(<I>t</I><SUB>j-1</SUB>))(<I>t</I><SUB>j</SUB> - <I>t</I><SUB>j-1</SUB>) + <I>&#963;</I>((t_j - t_j-1)X(t_j-1))<SUP>1/2</SUP>&nbsp;<I>Z</I><SUB>j</SUB>
 * </DIV><P></P>
 * where 
 * <SPAN CLASS="MATH"><I>Z</I><SUB>j</SUB>&#8764;<I>N</I>(0, 1)</SPAN>. This is a good approximation only for small
 * time intervals 
 * <SPAN CLASS="MATH"><I>t</I><SUB>j</SUB> - <I>t</I><SUB>j-1</SUB></SPAN>.
 * 
 */
public class CIRProcessEuler extends StochasticProcess  {
    protected NormalGen    gen;
    protected double       alpha,
                           beta,
                           sigma;
    // Precomputed values
    protected double[]     alphadt,
                           sigmasqrdt;



   /**
    * Constructs a new <TT>CIRProcessEuler</TT> with parameters
    * <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN> <TT>alpha</TT>, <SPAN CLASS="MATH"><I>b</I></SPAN>, <SPAN CLASS="MATH"><I>&#963;</I> =</SPAN> <TT>sigma</TT> and initial value
    * 
    * <SPAN CLASS="MATH"><I>X</I>(<I>t</I><SUB>0</SUB>) =</SPAN> <TT>x0</TT>. The normal variates <SPAN CLASS="MATH"><I>Z</I><SUB>j</SUB></SPAN> will be
    * generated by inversion using the stream <TT>stream</TT>.
    * 
    */
   public CIRProcessEuler (double x0, double alpha, double b, double sigma,
                           RandomStream stream)  {
      this (x0, alpha, b, sigma, new NormalGen (stream, new NormalDist()));
   }


   /**
    * The normal variate generator <TT>gen</TT> is specified directly
    * instead of specifying the stream.
    *  <TT>gen</TT> can use another method than inversion.
    * 
    */
   public CIRProcessEuler (double x0, double alpha, double b, double sigma,
                           NormalGen gen)  {
      this.alpha = alpha;
      this.beta  = b;
      this.sigma = sigma;
      this.x0    = x0;
      this.gen   = gen;
   }


   public double nextObservation() {
      double xOld = path[observationIndex];
      double x;
      x = xOld + (beta - xOld) * alphadt[observationIndex]
           + sigmasqrdt[observationIndex] * Math.sqrt(xOld) * gen.nextDouble();
      observationIndex++;
      if (x >= 0.0)
         path[observationIndex] = x;
      else
         path[observationIndex] = 0.;
      return x;
   }


   /**
    * Generates and returns the next observation at time <SPAN CLASS="MATH"><I>t</I><SUB>j+1</SUB> =</SPAN>
    *  <TT>nextTime</TT>, using the previous observation time <SPAN CLASS="MATH"><I>t</I><SUB>j</SUB></SPAN> defined earlier
    * (either by this method or by <TT>setObservationTimes</TT>),
    * as well as the value of the previous observation <SPAN CLASS="MATH"><I>X</I>(<I>t</I><SUB>j</SUB>)</SPAN>.
    * <SPAN  CLASS="textit">Warning</SPAN>: This method will reset the observations time <SPAN CLASS="MATH"><I>t</I><SUB>j+1</SUB></SPAN>
    * for this process to <TT>nextTime</TT>. The user must make sure that
    * the <SPAN CLASS="MATH"><I>t</I><SUB>j+1</SUB></SPAN> supplied is 
    * <SPAN CLASS="MATH">&nbsp;&gt;=&nbsp;<I>t</I><SUB>j</SUB></SPAN>.
    * 
    */
   public double nextObservation (double nextTime)  {
      double previousTime = t[observationIndex];
      double xOld = path[observationIndex];
      observationIndex++;
      t[observationIndex] = nextTime;
      double dt = nextTime - previousTime;
      double x = xOld + alpha * (beta - xOld) * dt
           + sigma * Math.sqrt (dt*xOld) * gen.nextDouble();
      if (x >= 0.0)
         path[observationIndex] = x;
      else
         path[observationIndex] = 0.;
      return x;
   }


   /**
    * Generates an observation of the process in <TT>dt</TT> time units,
    * assuming that the process has value <SPAN CLASS="MATH"><I>x</I></SPAN> at the current time.
    * Uses the process parameters specified in the constructor.
    * Note that this method does not affect the sample path of the process
    * stored internally (if any).
    * 
    * 
    */
   public double nextObservation (double x, double dt)  {
      x = x + alpha * (beta - x) * dt +
          sigma * Math.sqrt (dt*x) * gen.nextDouble();
      if (x >= 0.0)
         return x;
      return 0.0;
   }


   public double[] generatePath() {
      double x;
      double xOld = x0;
      for (int j = 0; j < d; j++) {
          x = xOld + (beta - xOld) * alphadt[j]
              + sigmasqrdt[j] * Math.sqrt(xOld) * gen.nextDouble();
          if (x < 0.0)
              x = 0.0;
          path[j + 1] = x;
          xOld = x;
      }
      observationIndex = d;
      return path;
   }

   public double[] generatePath (RandomStream stream) {
        gen.setStream (stream);
        return generatePath();
   }


   /**
    * Resets the parameters 
    * <SPAN CLASS="MATH"><I>X</I>(<I>t</I><SUB>0</SUB>) =</SPAN> <TT>x0</TT>, <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN> <TT>alpha</TT>,
    *  <SPAN CLASS="MATH"><I>b</I> =</SPAN> <TT>b</TT> and <SPAN CLASS="MATH"><I>&#963;</I> =</SPAN> <TT>sigma</TT> of the process.
    * <SPAN  CLASS="textit">Warning</SPAN>: This method will recompute some quantities stored internally,
    * which may be slow if called too frequently.
    * 
    */
   public void setParams (double x0, double alpha, double b, double sigma)  {
      this.alpha = alpha;
      this.beta  = b;
      this.sigma = sigma;
      this.x0    = x0;
      if (observationTimesSet) init(); // Otherwise not needed.
   }


   /**
    * Resets the random stream of the normal generator to <TT>stream</TT>.
    * 
    */
   public void setStream (RandomStream stream)  {
      gen.setStream (stream);
   }


   /**
    * Returns the random stream of the normal generator.
    * 
    */
   public RandomStream getStream()  {
      return gen.getStream ();
   }


   /**
    * Returns the value of <SPAN CLASS="MATH"><I>&#945;</I></SPAN>.
    * 
    */
   public double getAlpha()  { return alpha; }


   /**
    * Returns the value of <SPAN CLASS="MATH"><I>b</I></SPAN>.
    * 
    */
   public double getB()  { return beta; }


   /**
    * Returns the value of <SPAN CLASS="MATH"><I>&#963;</I></SPAN>.
    * 
    */
   public double getSigma()  { return sigma; }


   /**
    * Returns the normal random variate generator used.
    * The <TT>RandomStream</TT> used for that generator can be changed via
    * <TT>getGen().setStream(stream)</TT>, for example.
    * 
    */
   public NormalGen getGen()  { return gen; }
 

    // This is called by setObservationTimes to precompute constants
    // in order to speed up the path generation.
    protected void init() {
       super.init();
       alphadt = new double[d];
       sigmasqrdt = new double[d];
       double dt;
       for (int j = 0; j < d; j++) {
           dt = t[j+1] - t[j];
           alphadt[j]      = alpha * (dt);
           sigmasqrdt[j]   = sigma * Math.sqrt (dt);
       }
    }

} 
