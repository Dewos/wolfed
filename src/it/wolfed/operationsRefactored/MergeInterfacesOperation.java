package it.wolfed.operationsRefactored;

import it.wolfed.model.InterfaceVertex;
import it.wolfed.model.PetriNetGraph;
import it.wolfed.model.PlaceVertex;
import it.wolfed.model.Vertex;

/**
 * MergeInterfaces Operation.
 */
public class MergeInterfacesOperation extends Operation
{
    PetriNetGraph firstGraph;
    PetriNetGraph secondGraph;
    
    /**
     * @param operationGraph
     * @param firstGraph
     * @param secondGraph
     * @throws Exception  
     */
    public MergeInterfacesOperation(PetriNetGraph operationGraph, PetriNetGraph firstGraph, PetriNetGraph secondGraph) throws Exception
    {
        super(operationGraph);
        this.firstGraph = getIfIsWorkFlow(firstGraph);
        this.secondGraph = getIfIsWorkFlow(secondGraph);
        this.operationGraph = (new MergeGraphsOperation(operationGraph, firstGraph, secondGraph)).getOperationGraph();
        execute();
    }
   
    /**
     * Process Merge Interfaces.
     * 
     * FistGraph:      
     *                 I1
     *                  ^
     *                  |
     *            P1 -> T1 -> P2
     * 
     * -------------------------
     * 
     * SecondGraph:
     * 
     *            P3 -> T2 -> P3
     *                  |
     *                  I1
     * 
     * -------------------------
     * ResultGraph:
     * 
     */
    @Override
    void process() throws Exception
    {
        int countInterfaces = 0;
        
        // Finds all interfaces in first
        for(Object cellObj : firstGraph.getChildVertices())
        {
            if(cellObj instanceof InterfaceVertex)
            {
                InterfaceVertex interfFirst = (InterfaceVertex) cellObj;
                InterfaceVertex interfSecond = (InterfaceVertex) secondGraph.getVertexByValue(interfFirst.getValue());
                
                // Matching exists?
                if(interfSecond != null)
                {
                    countInterfaces++;
                    
                    // Matching first and second found! Merge the same interface in op
                    Vertex interfAsFirst = getEquivalentVertex(1, interfFirst);
                    Vertex interfAsSecond = getEquivalentVertex(2, interfSecond);
                    
                    // Mirror a place instead the interface
                    PlaceVertex placeInterf = operationGraph.insertPlace((String) interfFirst.getValue());
                    cloneIncomingEdges(interfAsFirst, placeInterf);
                    cloneEdges(interfAsSecond, interfAsSecond);

                    // Remove
                    removeVertexAndHisEdges(interfAsFirst);
                    removeVertexAndHisEdges(interfAsSecond);
                }
            }
        }
        
        if(countInterfaces > 0)
        {
            // Make System
            operationGraph = (new ParallelismOperation(operationGraph, firstGraph, secondGraph)).getOperationGraph();
        }
        else
        {
            throw new Exception("No common interfaces found in the two graphs.");
        }
    }
}