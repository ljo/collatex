package eu.interedition.collatex2.experimental.vgalignment;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.alignment.AlignmentTest;
import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.VariantGraph2;
import eu.interedition.collatex2.experimental.vg_alignment.IAlignment2;
import eu.interedition.collatex2.experimental.vg_alignment.IMatch2;
import eu.interedition.collatex2.experimental.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

//NOTE: these unit tests replace the tests in AlignmentTest!
//TODO: have to be rewritten to work with VariantGraph alignment!
public class VGAlignmentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AlignmentTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testSimple1() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "a c b");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<IMatch2> matches = alignment.getMatches();
    assertEquals(2, matches.size());
    assertEquals("a", matches.get(0).getNormalized());
    assertEquals("b", matches.get(1).getNormalized());
  }
  
  //Copied from TextAlignmentTest
  @Test
  public void testAlignment() {
    final IWitness a = factory.createWitness("A", "cat");
    final IWitness b = factory.createWitness("B", "cat");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<IMatch2> matches = alignment.getMatches();
    assertEquals(1, matches.size());
    assertEquals("cat", matches.get(0).getNormalized());
  }

  @Test
  public void testAlignment2Matches() {
    final IWitness a = factory.createWitness("A", "The black cat");
    final IWitness b = factory.createWitness("B", "The black and white cat");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<IMatch2> matches = alignment.getMatches();
    assertEquals(2, matches.size());
    assertEquals("the black", matches.get(0).getNormalized());
    assertEquals("cat", matches.get(1).getNormalized());
  }

  // Note: taken from TextAlignmentTest!
  @Test
  public void testAddition_AtTheStart() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "not to be");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<IMatch2> matches = alignment.getMatches();
    assertEquals(1, matches.size());
    assertEquals("to be", matches.get(0).getNormalized());
  }


}