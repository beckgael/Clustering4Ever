package lipn.spartakus.core.math.distances

import _root_.scala.math.pow
import _root_.scala.util.Try

class Minkowski(p: Int) extends ContinuousDistances
{
	/**
	  * The famous Minkowski distance implemented
	  * @return The Minkowski distance between dot1 and dot2
	  * @param p : Minkowsiki parameter
	  **/
	override def distance(dot1: Seq[Double], dot2: Seq[Double]): Double =
	{
		pow( ( for( i <- 0 until dot1.size ) yield( pow(dot1(i) - dot2(i), p) ) ).reduce(_ + _), 1D / p )
	}
}


