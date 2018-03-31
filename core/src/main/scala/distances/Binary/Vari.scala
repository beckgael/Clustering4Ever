package clustering4ever.math.distances.binary

import _root_.clustering4ever.math.distances.{BinaryDistance, BinaryDistanceUtil}
import _root_.scala.math.pow

/**
 * @author Beck Gaël
 **/
class Vari extends BinaryDistance
{
	override def distance(vector1: Seq[Int], vector2: Seq[Int]): Double =
	{
		val (a,b,c,d) = BinaryDistanceUtil.contingencyTable(vector1, vector2)
		(b + c).toDouble / (4 * (a + b + c + d))
	}
}