package supplyChain_ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class SupplyChainOntology extends BeanOntology {

	private static Ontology thisInstance = new SupplyChainOntology("ontology");
	
	public static Ontology getInstance() {
		return thisInstance;
	}
	
	private SupplyChainOntology (String name) {
		super(name);
		try {
			add("supplyChain_ontology.elements");
		}
		catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}
}
