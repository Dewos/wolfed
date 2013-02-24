
package it.wolfed.model;

import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import it.wolfed.util.Constants;
import it.wolfed.util.IterableNodeList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.w3c.dom.Node;

/**
 * PetriNet implementation.
 * 
 * The class extends the visual base model mxGraph for Jgraphx library.
 * 
 * Petri graph definition:
 * A Petri graph is a directed bipartite graph, in which the nodes represent 
 * transitions (i.e. events that may occur, signified by bars) and places 
 * (i.e. conditions, signified by circles). 
 * 
 * A Petri graph consists of places, transitions, arcs and interfaces. 
 * Arcs run from a place to a transition or vice versa, never between places
 * or between transitions. 
 * 
 * The places from which an arc runs to a transition are called the input places
 * of the transition; the places to which arcs run from a transition are called
 * the output places of the transition.
 * 
 * @see http://en.wikipedia.org/wiki/Petri_net
 * @see https://github.com/jgraph/jgraphx
 */
public class PetriNetGraph extends mxGraph
{
    /**
     * Id of the Graph.
     * 
     * Usually id from pnml file or tab name.
     */
    private String id;
    
    /**
     * Type for the graph.
     */
    private String type;  
    
    /**
     * Index of the places of the graph.
     * 
     * Must be in sync with any new place creation.
     */
    private int indexPlaces;
    
    /**
     * Index of the transitions of the graph.
     * 
     * Must be in sync with any new transition creation.
     */
    private int indexTransitions;
    
    /**
     * Index of the arcs of the graph.
     * 
     * Must be in sync with any new arc creation.
     */
    private int indexArcs;
    
    /**
     * Index of the interfaces of the graph.
     * 
     * Must be in sync with any interface creation.
     */
    private int indexInterfaces;

    /**
     * Constructor.
     * 
     * @param id
     */
    public PetriNetGraph(String id)
    {
        this.id = id;
        
        setCellsResizable(false);
        setAllowDanglingEdges(false);
        setAllowLoops(false);
        setDropEnabled(false);
        setMultigraph(false);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public String toString()
    {
        return id;
    }
    
    /**
     * Imports a pnml "net" node in a new PetriNetGraph.
     * 
     * @param dom
     * @param defaultId
     * @return PetriNetGraph
     */
    public static PetriNetGraph factory(Node dom, String defaultId)
    {
        // Xml Mapping 1:1
        String id = dom.getAttributes().getNamedItem(Constants.PNML_ID).getTextContent().trim();
        String type = dom.getAttributes().getNamedItem(Constants.PNML_TYPE).getTextContent().trim();
        
        // Fix Woped "noID" behaviour
        if(id.isEmpty() || id.equals("noID"))
        {
            id = defaultId;
        }
        
        // Creates new Graph
        PetriNetGraph graph = new PetriNetGraph(id);
        graph.setType(type);
                
        // Pmnl Import
        graph.getModel().beginUpdate();
        Object parent = graph.getDefaultParent();

        try
        {
            for (final Node elementNode : new IterableNodeList(dom.getChildNodes())) 
            {
                if (elementNode.getNodeType() == Node.ELEMENT_NODE) 
                {
                    switch (elementNode.getNodeName())
                    {
                        case Constants.PNML_PLACE:
                            graph.addCell(PlaceVertex.factory(parent, elementNode));
                            graph.nextIndexPlaces();
                            break;

                        case Constants.PNML_TRANSITION:
                            graph.addCell(TransitionVertex.factory(parent, elementNode));
                            graph.nextIndexTransition();
                            break;

                        case Constants.PNML_ARC:
                            ArcEdge arc = ArcEdge.factory(parent, elementNode);
                            Vertex source = graph.getVertexById(arc.getSourceId());
                            Vertex target = graph.getVertexById(arc.getTargetId());
                            graph.addEdge(arc, parent, source, target, null);
                            break;
                    }
                }
            }
        }
        finally
        {
            graph.getModel().endUpdate();
        }
        
        return graph;
    }

    /**
     * Returns graph id.
     * 
     * @return String
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * Returns graph type.
     * 
     * @return String
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets graph type.
     * 
     * @param type 
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Increments and returns the current places id.
     * 
     * @return String
     */
    public String nextIndexPlaces()
    {
        return "p" + String.valueOf(++indexPlaces);
    }

    /**
     * Increments and returns the current transition index.
     * 
     * @return String
     */
    public String nextIndexTransition()
    {
        return "t" + String.valueOf(++indexTransitions);
    }
    
    
    /**
     * Increments and returns the current arcs index.
     * 
     * @return String
     */
    public String nextIndexArcs()
    {
        return "a" + String.valueOf(++indexArcs);
    }
    
    /**
     * Increments and returns the current interface index.
     * 
     * @return String
     */
    public String nextIndexInterfaces()
    {
        return "i" + String.valueOf(++indexInterfaces);
    }

    /**
     * Force only-vertex selectable cells.
     * 
     * @param cell 
     * @return boolean
     */
    @Override
    public boolean isCellSelectable(Object cell) 
    {
         if (cell != null) 
         {
            if (cell instanceof mxCell) 
            {
               mxCell myCell = (mxCell) cell;
               
               if (myCell.isEdge())
                {
                    return false;
                }
            }
         }
         
         return super.isCellSelectable(cell);
    }
    
    /**
     * Returns if the graph has only an initial place.
     * 
     * @return boolean
     */
    public boolean isSingleInitialPlace()
    {
        return (getInitialPlaces().size() == 1);
    }
    
    /**
     * Returns if the graph has only an final place.
     * 
     * @return boolean
     */
    public boolean isSingleFinalPlace()
    {
        return (getFinalPlaces().size() == 1);
    }
    
    /**
     * Returns if a workflow has a path from initial to final place for each vertex.
     * 
     * @return boolean
     */
    public boolean isWorkflowStronglyConnected()
    {
        if(isSingleInitialPlace() == false || isSingleFinalPlace() == false)
        {            
            return false;
        }
        
        PlaceVertex finalplace = getFinalPlaces().iterator().next();
        HashSet<Vertex> notConnectedVertex = getNotConnectedVertices(finalplace);

        return notConnectedVertex.isEmpty();
    }
    
    /**
     * Returns if the graph is a workflow net.
     * 
     * Properties:
     * 
     * Single initial place
     * Single final place
     * Exist for each vertex a path to the final place.
     * 
     * @return boolean
     */
    public boolean isWorkFlow()
    {
        //return isSingleInitialPlace() && isSingleFinalPlace() && isWorkflowStronglyConnected();
        return isWorkflowStronglyConnected();
    }
    
    /**
     * Returns all the Places and Transition without a path to target Vertex.
     * 
     * @param target
     * @return HashSet<Vertex>
     */
    public HashSet<Vertex> getNotConnectedVertices(Vertex target)
    {
        HashSet<Vertex> notConnectedVertices = new HashSet<>();
        
        for (Object cell : getChildVertices())
        {
            /**
             * Search a path to target.
             * 
             * @todo Change this with a siple path check (shortestpath now)
             */
            if(cell != target)
            {
                Object[] paths = mxGraphAnalysis
                    .getInstance()
                    .getShortestPath(
                        this,
                        cell,
                        target,
                        new mxDistanceCostFunction(),
                        getChildVertices().length,
                        true
                 );

                // Path not exists
                if(paths.length == 0)
                {
                    // Ignore Interfaces
                    if(cell instanceof TransitionVertex || cell instanceof PlaceVertex)
                    {
                        notConnectedVertices.add((Vertex) cell);
                    }
                }
            }
            
            // Case with a Transition "Initial" (only postset arc)
            if(cell instanceof TransitionVertex 
                    && getIncomingEdges(cell).length == 0)
            {
                notConnectedVertices.add((Vertex) cell);
            }
        }
        
        return notConnectedVertices;
    }
    
    /**
     * Get a vertex by his id.
     * 
     * @param  id 
     * @return Vertex
     */
    public Vertex getVertexById(String id)
    {
        for (Object objVertex : getChildVertices())
        {
            if (objVertex instanceof Vertex)
            {
                Vertex vertex = (Vertex) objVertex;

                if (vertex.getId().equals(id))
                {
                    return vertex;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the initial places of the graph.
     * 
     * An initial place is a place with:
     * 
     * #Preset arcs : 0
     * #Postset arcs: > 1
     * 
     * @return List<PlaceVertex>
     */
    public List<PlaceVertex> getInitialPlaces()
    {
        List<PlaceVertex> initialPlaces = new ArrayList<>();
        
        for (Object vertexObj : getChildVertices())
        {   
            if(vertexObj instanceof PlaceVertex)
            {
                if(getIncomingEdges(vertexObj).length == 0
                    && getOutgoingEdges(vertexObj).length > 0)
                {
                    initialPlaces.add((PlaceVertex) vertexObj);
                }
            }
        }
        
        return initialPlaces;
    }
    
    /**
     * Get the final places of the graph.
     * 
     * A final place is a place with:
     * 
     * #Preset arcs : > 1
     * #Postset arcs: 0
     * 
     * @return List<PlaceVertex>
     */
    public List<PlaceVertex> getFinalPlaces()
    {
        List<PlaceVertex> finalPlaces = new ArrayList<>();
        
        for (Object vertexObj : getChildVertices())
        {   
            if(vertexObj instanceof PlaceVertex)
            {
                if(getOutgoingEdges(vertexObj).length == 0
                    && getIncomingEdges(vertexObj).length > 0)
                {
                    finalPlaces.add((PlaceVertex) vertexObj);
                }
            }    
        }

        return finalPlaces;
    }
    
    /**
     * Get child cell of the default graph parent.
     * 
     * @return Object[]
     */
    public Object[] getChildCells()
    {
        return getChildCells(getDefaultParent());
    }
    
    /**
     * Get child vertices of the default graph parent.
     * 
     * @return Object[]
     */
    public Object[] getChildVertices()
    {
        return getChildVertices(getDefaultParent());
    }
    
    /**
     * Get child edge of the default graph parent.
     * 
     * @return Object[]
     */
    public Object[] getChildEdges()
    {
        return getChildEdges(getDefaultParent());
    }
    
    /**
     * Add a new Place to the graph.
     * 
     * @todo    method overload for id
     * @param   id
     * @return  PlaceVertex
     */
    public PlaceVertex insertPlace(String id)
    {
        if(id == null)
        {
           id = nextIndexPlaces();
        }
        
        PlaceVertex place = new PlaceVertex(getDefaultParent(), id, id);
        return (PlaceVertex) addCell(place);
    }
    
    /**
     * Add a new Transition to the graph.
     * 
     * @todo    method overload for id
     * @param   id
     * @return  TransitionVertex
     */
    public TransitionVertex insertTransition(String id)
    {
        if(id == null)
        {
            id = nextIndexTransition();
        }
        
        TransitionVertex transition = new TransitionVertex(getDefaultParent(), id, id);
        return (TransitionVertex) addCell(transition);
    }
    
    /**
     * Add a new Arc to the graph.
     * 
     * @todo    method overload for id
     * @param id
     * @param source 
     * @param target 
     * @return 
     */
    public ArcEdge insertArc(String id, Vertex source, Vertex target)
    {
        if(id == null)
        {
            id = nextIndexArcs();
        }
        
        ArcEdge edge = new ArcEdge(getDefaultParent(), id, "");
        edge.setSourceId(source.getId());
        edge.setTargetId(target.getId());
        
        return (ArcEdge) addEdge(edge, getDefaultParent(), source, target, null);
    }
    
    /**
     * Prints out some useful information about the cell in the tooltip.
     * 
     * @param   cell
     * @return  String
     */
    @Override
    public String getToolTipForCell(Object cell)
    {
        String tip = "";	
        mxCell mxcell = (mxCell) cell;
        
        tip += mxcell.getClass();
        tip += " | ID: " + mxcell.getId();
        tip += " | VALUE: " + mxcell.getValue();
        
        if(cell instanceof Vertex)
        {
            tip += " | PRESET: " + String.format("%d", getIncomingEdges(cell).length);
            tip += " | POSTSET: " + String.format("%d", getOutgoingEdges(cell).length);
            
            if(cell instanceof PlaceVertex)
            {
                PlaceVertex p = (PlaceVertex) cell;
                tip += " | TOKENS: " + String.format("%d", p.getTokens());
            }
        }

        return tip;
    }
}