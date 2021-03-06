package clustering4ever.scala.clustering.kmeans

import _root_.clustering4ever.clustering.datasetstype.DataSetsTypes
import _root_.clustering4ever.clustering.ClusteringAlgorithms
import _root_.clustering4ever.math.distances.ContinuousDistances
import _root_.clustering4ever.util.SumArrays
import _root_.scala.math.{min, max}
import _root_.scala.collection.{immutable, mutable}
import _root_.scala.util.Random

/**
 * @author Beck Gaël
 * The famous K-Means using a user-defined dissmilarity measure.
 * @param data : an Array with and ID and the vector
 * @param k : number of clusters
 * @param epsilon : minimal threshold under which we consider a centroid has converged
 * @param iterMax : maximal number of iteration
 * @param metric : a defined dissimilarity measure, it can be custom by overriding ContinuousDistances distance function
 **/
class KMeans(
	data: Seq[(Int, Array[Double])],
	var k: Int,
	var epsilon: Double,
	var iterMax: Int,
	var metric: ContinuousDistances
) extends ClusteringAlgorithms[Int, Double]
{
	val dim = data.head._2.size

	/**
	 * Simplest centroids initializations
	 * We search range for each dimension and take a random value between each range 
	 **/
	def initializationCentroids =
	{
		val vectorRange = (0 until dim).toArray

		def obtainMinMax(idx: Int, vminMax1: (Array[Double], Array[Double]), vminMax2: (Array[Double], Array[Double])) =
		{
			(
				min(vminMax1._1(idx), vminMax2._1(idx)),
				max(vminMax1._2(idx), vminMax2._2(idx))
			)
		}

		val (minv, maxv) = data.map{ case (_, v) => (v, v) }.reduce( (minMaxa, minMaxb) =>
		{
			val minAndMax = for( i <- vectorRange ) yield( obtainMinMax(i, minMaxa, minMaxb) )
			minAndMax.unzip
		})

		val ranges = minv.zip(maxv).map{ case (min, max) => (max - min, min) }
		val centroids = mutable.HashMap((0 until k).map( clusterID => (clusterID, ranges.map{ case (range, min) => Random.nextDouble * range + min }) ):_*)
		centroids
	}

	/**
	 * Run the K-Means
	 **/
	def run(): KMeansModel =
	{
		val centroids = initializationCentroids
		val clustersCardinality = centroids.map{ case (clusterID, _) => (clusterID, 0) }

		def obtainNearestModID(v: Array[Double]): ClusterID = centroids.toArray.map{ case(clusterID, mod) => (clusterID, metric.d(mod, v)) }.sortBy(_._2).head._1

		val zeroMod = Array.fill(dim)(0D)
		var cpt = 0
		var allModsHaveConverged = false
		while( cpt < iterMax && ! allModsHaveConverged )
		{
			// Allocation to nearest centroid
			val clusterized = data.map{ case (id, v) => (id, v, obtainNearestModID(v)) }

			val kModesBeforeUpdate = centroids.clone

			// Reinitialization of centroids
			centroids.foreach{ case (clusterID, mod) => centroids(clusterID) = zeroMod }
			clustersCardinality.foreach{ case (clusterID, _) => clustersCardinality(clusterID) = 0 }

			// Updatating Modes
			clusterized.foreach{ case (_, v, clusterID) =>
			{
				centroids(clusterID) = SumArrays.sumArraysNumerics(centroids(clusterID), v)
				clustersCardinality(clusterID) += 1
			}}

			centroids.foreach{ case (clusterID, mod) => centroids(clusterID) = mod.map(_ / clustersCardinality(clusterID)) }

			allModsHaveConverged = kModesBeforeUpdate.forall{ case (clusterID, previousMod) => metric.d(previousMod, centroids(clusterID)) <= epsilon }

			cpt += 1
		}

		new KMeansModel(centroids, clustersCardinality, metric)
	}
}

object KMeans extends DataSetsTypes[Int, Double]
{
	/**
	 * Run the K-Means
	 **/
	def run(data: Array[(ID, Vector)], k: Int, epsilon: Double, iterMax: Int, metric: ContinuousDistances): KMeansModel =
	{
		val kMeans = new KMeans(data, k, epsilon, iterMax, metric)
		val kmeansModel = kMeans.run()
		kmeansModel
	}
}