

/*
 * Class:        AndersonDarlingDist
 * Description:  Anderson-Darling distribution
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

package umontreal.iro.lecuyer.probdist;

import umontreal.iro.lecuyer.util.*;
import umontreal.iro.lecuyer.functions.MathFunction;
 

/**
 * Extends the class {@link ContinuousDistribution} for the
 * <EM>Anderson-Darling</EM>  distribution (see).
 * Given a sample of <SPAN CLASS="MATH"><I>n</I></SPAN> independent uniforms <SPAN CLASS="MATH"><I>U</I><SUB>i</SUB></SPAN> over <SPAN CLASS="MATH">(0, 1)</SPAN>,
 * the <EM>Anderson-Darling</EM> statistic <SPAN CLASS="MATH"><I>A</I><SUB>n</SUB><SUP>2</SUP></SPAN> is defined by
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>A</I><SUB>n</SUB><SUP>2</SUP> = - <I>n</I> - 1/<I>n</I>&sum;<SUB>j=1</SUB><SUP>n</SUP>{(2<I>j</I> - 1)ln(<I>U</I><SUB>(j)</SUB>) + (2<I>n</I> + 1 - 2<I>j</I>)ln(1 - <I>U</I><SUB>(j)</SUB>)},
 * </DIV><P></P>
 * where the <SPAN CLASS="MATH"><I>U</I><SUB>(j)</SUB></SPAN> are the <SPAN CLASS="MATH"><I>U</I><SUB>i</SUB></SPAN> sorted in increasing order. The 
 * distribution function (the cumulative probabilities)
 *  is defined as 
 * <SPAN CLASS="MATH"><I>F</I><SUB>n</SUB>(<I>x</I>) = <I>P</I>[<I>A</I><SUB>n</SUB><SUP>2</SUP>&nbsp;&lt;=&nbsp;<I>x</I>]</SPAN>.
 * 
 */
public class AndersonDarlingDist extends ContinuousDistribution {
   protected int n;

   private static class Function implements MathFunction {
      protected int n;
      protected double u;

      public Function (int n, double u) {
         this.n = n;
         this.u = u;
      }

      public double evaluate (double x) {
         return u - cdf(n,x);
      }
   }



   /**
    * Constructs an <EM>Anderson-Darling</EM> distribution for a sample of size <SPAN CLASS="MATH"><I>n</I></SPAN>.
    * 
    */
   public AndersonDarlingDist (int n) {
      setN (n);
   }


   public double density (double x) {
      return density (n, x);
   }

   public double cdf (double x) {
      return cdf (n, x);
   }

   public double barF (double x) {
      return barF (n, x);
   }

   public double inverseF (double u) {
      return inverseF (n, u);
   }

   private static double dclem (int n, double x, double EPS) {
      return (cdf(n, x + EPS) - cdf(n, x - EPS)) / (2.0 * EPS);
   }

   protected static double density_N_1 (double x)
   {
      final double AD_X0 = 0.38629436111989062;
      final double AD_X1 = 37.816242111357;
      if (x <= AD_X0 || x >= AD_X1)
         return 0.0;

      final double t = Math.exp (-x - 1.0);
      return 2.0 * t / Math.sqrt (1.0 - 4.0*t); 
   }

   /**
    * Computes the density of the <EM>Anderson-Darling</EM> distribution with parameter <SPAN CLASS="MATH"><I>n</I></SPAN>.
    * 
    */
   public static double density (int n, double x) {
      if (n <= 0)
         throw new IllegalArgumentException ("n <= 0");
      if (n == 1) 
         return density_N_1(x);

      if (x >= XBIG || x <= 0.0)
         return 0.0;
      final double EPS = 1.0 / 100.0;
      final double D1 = dclem(n, x, EPS);
      final double D2 = dclem(n, x, 2.0 * EPS);
      final double RES = D1 + (D1 - D2) / 3.0;
      if (RES <= 0.0)
         return 0.0;
      return RES;
   }

   protected static double cdf_N_1 (double x)
   {
      // The Anderson-Darling distribution for N = 1
      final double AD_X0 = 0.38629436111989062;
      final double AD_X1 = 37.816242111357;

      if (x <= AD_X0)
         return 0.0;
      if (x >= AD_X1)
         return 1.0;
      return Math.sqrt (1.0 - 4.0 * Math.exp (-x - 1.0));
   }

   private static double ADf (double z, int j)
   {                                 // called by ADinf(); see article.
      final double T = (4.0 * j + 1.0) * (4.0 * j + 1.0) * 1.23370055013617 / z;
      if (T > 150.)
         return 0.;

      double f, fnew, a, b, c, r;
      int i;
      a = 2.22144146907918 * Math.exp (-T) / Math.sqrt (T);
      // initialization requires cPhi
      // if you have erfc(), replace 2*cPhi(sqrt(2*t)) with erfc(sqrt(t))
      b = 3.93740248643060 * 2. * NormalDistQuick.barF01 (Math.sqrt (2 * T));
      r = z * .125;
      f = a + b * r;
      for (i = 1; i < 200; i++) {
         c = ((i - .5 - T) * b + T * a) / i;
         a = b;
         b = c;
         r *= z / (8 * i + 8);
         if (Math.abs (r) < 1e-40 || Math.abs (c) < 1.e-40)
            return f;
         fnew = f + c * r;
         if (f == fnew)
            return f;
         f = fnew;
      }
      return f;
   }

   private static double ADinf (double z)
   {
      if (z < .01)
         return 0.;   // avoids exponent limits; ADinf(.01)=.528e-52
      int j;
      double ad, adnew, r;
      r = 1. / z;
      ad = r * ADf (z, 0);
      for (j = 1; j < 100; j++) {
         r *= (.5 - j) / j;
         adnew = ad + (4 * j + 1) * r * ADf (z, j);
         if (ad == adnew) {
            return ad;
         }
         ad = adnew;
      }
      return ad;
   }

   private static double adinf (double z)
   {
      if (z < 2.)
         return Math.exp (-1.2337141 / z) / Math.sqrt (z) * (2.00012 + (.247105 -
               (.0649821 - (.0347962 - (.011672 -
                        .00168691 * z) * z) * z) * z) * z);
      // max |error| < .000002 for z<2, (p=.90816...)
      return
         Math.exp (-Math.exp (1.0776 - (2.30695 - (.43424 - (.082433 - 
                     (.008056 - .0003146 * z) * z) * z) * z) * z));
      // max |error|<.0000008 for 4<z<infinity
   }

   private static double AD (int n, double z, boolean isFastADinf)
   {
      double v, x;
      /* If isFastADinf is true, use the fast approximation adinf (z),
         if it is false, use the more exact ADinf (z) */
      if (isFastADinf)
         x = adinf (z);
      else
         x = ADinf (z);

      // now x=adinf(z). Next, get v=errfix(n,x) and return x+v;
      if (x > .8) {
         v = (-130.2137 + (745.2337 - (1705.091 - (1950.646 - (1116.360 -
                        255.7844 * x) * x) * x) * x) * x) / n;
         return x + v;
      }
      final double C = .01265 + .1757 / n;
      if (x < C) {
         v = x / C;
         v = Math.sqrt (v) * (1. - v) * (49 * v - 102);
         return x + v * (.0037 / (n * n) + .00078 / n + .00006) / n;
      }
      v = (x - C) / (.8 - C);
      v = -.00022633 + (6.54034 - (14.6538 - (14.458 - (8.259 -
               1.91864 * v) * v) * v) * v) * v;
      return x + v * (.04213 + .01365 / n) / n;
   }


   /**
    * Computes the <EM>Anderson-Darling</EM> distribution function <SPAN CLASS="MATH"><I>F</I><SUB>n</SUB>(<I>x</I>)</SPAN>, with parameter <SPAN CLASS="MATH"><I>n</I></SPAN>,
    *   using Marsaglia's and al. algorithm. First the asymptotic
    *   distribution for 
    * <SPAN CLASS="MATH"><I>n</I>&nbsp;-&gt;&nbsp;&#8734;</SPAN> is computed.
    *   Then an empirical correction obtained by simulation is added for finite <SPAN CLASS="MATH"><I>n</I></SPAN>.
    * 
    */
   public static double cdf (int n, double x) {
      if (n <= 0)
         throw new IllegalArgumentException ("n <= 0");
      if (x <= 0)
         return 0.0;
      if (x >= XBIG)
         return 1.0;
      if (1 == n)
         return cdf_N_1 (x);
      final double RES = AD (n, x, true);
      if (RES <= 0.0)
         return 0.0;
      return RES;
   }
 

   protected static double barF_N_1 (double x)
   {
      if (x <= 3.8629436111989E-1)
         return 1.0;
      if (x >= XBIGM)
         return 0.0;

      double q;
      if (x < 6.0) {
         q = 1.0 - 4.0 * Math.exp(-x - 1.0);
         return 1.0 - Math.sqrt (q);
      }
      q = 4.0 * Math.exp(-x - 1.0);
      return 0.5*q*(1.0 + 0.25*q*(1.0 + 0.5*q*(1.0 + 0.125*q*(5.0 + 3.5*q))));
   }

   /**
    * Computes the complementary  distribution function  
    * <SPAN CLASS="MATH">bar(F)<SUB>n</SUB>(<I>x</I>)</SPAN> 
    *   with parameter <SPAN CLASS="MATH"><I>n</I></SPAN>.
    * 
    */
   public static double barF (int n, double x) {
      if (n <= 0)
        throw new IllegalArgumentException ("n <= 0");
      if (n == 1)
         return barF_N_1 (x);
      return 1.0 - cdf (n, x);
   }

   protected static double inverse_N_1 (double u)
   {
      final double AD_X0 = 0.38629436111989062;
      if (u <= 0.0)
         return AD_X0;
      final double AD_X1 = 37.816242111357;
      if (u >= 1.0)
         return AD_X1;
      return AD_X0 - Math.log1p (-u*u);
   }

   /**
    * Computes the inverse 
    * <SPAN CLASS="MATH"><I>x</I> = <I>F</I><SUB>n</SUB><SUP>-1</SUP>(<I>u</I>)</SPAN> of the
    *   <EM>Anderson-Darling</EM> distribution with parameter <SPAN CLASS="MATH"><I>n</I></SPAN>.
    * 
    */
   public static double inverseF (int n, double u) {
      if (n <= 0)
         throw new IllegalArgumentException ("n <= 0");
      if (u < 0.0 || u > 1.0)
         throw new IllegalArgumentException ("u must be in [0,1]");
      if (n == 1)
         return inverse_N_1 (u);
      if (u == 1.0)
         return Double.POSITIVE_INFINITY;
      if (u == 0.0)
         return 0.0;
      Function f = new Function (n,u);
      return RootFinder.brentDekker (0.0, 50.0, f, 1e-10);
   }


   /**
    * Returns the parameter <SPAN CLASS="MATH"><I>n</I></SPAN> of this object.
    * 
    */
   public int getN() {
      return n;
   }


   /**
    * Sets the parameter <SPAN CLASS="MATH"><I>n</I></SPAN> of this object.
    * 
    */
   public void setN (int n) {
      if (n <= 0)
         throw new IllegalArgumentException ("n < 1");
      this.n = n;
      if (1 == n) {
         supportA = 0.38629436111989062;
         supportB = 37.816242111357;
      } else {
         supportA = 0.0;
         supportB = 1000.0;
      }
   }


   /**
    * Return an array containing the parameter <SPAN CLASS="MATH"><I>n</I></SPAN> of the current distribution.
    * 
    * 
    */
   public double[] getParams () {
      double[] retour = {n};
      return retour;
   }


   public String toString () {
      return getClass().getSimpleName() + " : n = " + n;
   }

}
