package it.wolfed.operations;

import it.wolfed.model.PetriNetGraph;
import it.wolfed.model.PlaceVertex;
import it.wolfed.model.TransitionVertex;
import it.wolfed.model.Vertex;

import java.util.List;

public class ParallelismOperation extends Operation{
	public ParallelismOperation(List<PetriNetGraph> inputGraphs) throws Exception {
		super("par", inputGraphs, 2, true);
	}

	/*
	 * Parallelism
	 * (AND-split + AND-join)
	 * A and B are both executed in no particular order
	 *       			-> p1 -> A -> p3 ->  
	 *  i -> AND-split						AND-join -> o
	 *       			-> p2 -> B -> p4 ->
	 *       
	 *      where AND-split, AND-join are Transitions
	 *      and i, p1, p2, p3, p4 and o are places
	 */
	@Override
	void process() {
		
		insertInitialPattern();
		insertFinalPattern();
		
//		PlaceVertex pi = getOperationGraph().insertPlace("initial");
//		PlaceVertex po = getOperationGraph().insertPlace("final");
//		
//		TransitionVertex andSplit = getOperationGraph().insertTransition("AND-split");
//		TransitionVertex andJoin = getOperationGraph().insertTransition("AND-join");
//		
//		// TODO Auto-generated method stub
//		PetriNetGraph net0 = getInputGraphs().get(0);
//		PetriNetGraph net1 = getInputGraphs().get(1);
//		
//		Vertex initialPlaceAsN0 = getEquivalentVertex(net0, net0.getInitialPlaces().get(0));
//		Vertex finalPlaceAsN0 = getEquivalentVertex(net0, net0.getFinalPlaces().get(0));
//		
//		Vertex initialPlaceAsN1 = getEquivalentVertex(net1, net1.getInitialPlaces().get(0));
//		Vertex finalPlaceAsN1 = getEquivalentVertex(net1, net1.getFinalPlaces().get(0));
		
		
	}

	/*
	 *        			-> p1 -> A -> p3 ->  
	 *  i -> AND-split						AND-join -> o
	 *       			-> p2 -> B -> p4 ->
	 */
	
	private void insertInitialPattern() {
		// TODO Auto-generated method stub
		PlaceVertex pi = getOperationGraph().insertPlace("initial");
		TransitionVertex andSplit = getOperationGraph().insertTransition("AND-split");
		
		// TODO Auto-generated method stub
		PetriNetGraph net0 = getInputGraphs().get(0);
		PetriNetGraph net1 = getInputGraphs().get(1);
		Vertex initialPlaceAsN0 = getEquivalentVertex(net0, net0.getInitialPlaces().get(0));
		Vertex initialPlaceAsN1 = getEquivalentVertex(net1, net1.getInitialPlaces().get(0));
		
		
		getOperationGraph().insertArc(null, pi, andSplit);
		getOperationGraph().insertArc(null, andSplit, initialPlaceAsN0);
		getOperationGraph().insertArc(null, andSplit, initialPlaceAsN1);
	}
	/*
	 *        			-> p1 -> A -> p3 ->  
	 *  i -> AND-split						AND-join -> o
	 *       			-> p2 -> B -> p4 ->
	 */
	private void insertFinalPattern() {
		// TODO Auto-generated method stub
		PlaceVertex po = getOperationGraph().insertPlace("final");
		TransitionVertex andJoin = getOperationGraph().insertTransition("AND-join");
		// TODO Auto-generated method stub
		PetriNetGraph net0 = getInputGraphs().get(0);
		PetriNetGraph net1 = getInputGraphs().get(1);
		Vertex finalPlaceAsN0 = getEquivalentVertex(net0, net0.getFinalPlaces().get(0));
		Vertex finalPlaceAsN1 = getEquivalentVertex(net1, net1.getFinalPlaces().get(0));
		
		getOperationGraph().insertArc(null, andJoin, po);
		getOperationGraph().insertArc(null, finalPlaceAsN0, andJoin);
		getOperationGraph().insertArc(null, finalPlaceAsN1, andJoin);
		
	}
}