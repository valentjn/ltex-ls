package org.bsplines.ltex_ls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IgnoreRuleSentencePairTest {
  @Test
  public void doTest() {
    IgnoreRuleSentencePair pair = new IgnoreRuleSentencePair("a", "b");
    Assertions.assertEquals(pair, new IgnoreRuleSentencePair(pair));
    Assertions.assertNotEquals(pair, new IgnoreRuleSentencePair("X", "b"));
    Assertions.assertNotEquals(pair, new IgnoreRuleSentencePair("a", "X"));
    Assertions.assertDoesNotThrow(() -> pair.hashCode());
    Assertions.assertEquals("a", pair.getRuleId());
    Assertions.assertEquals("b", pair.getSentenceString());
    Assertions.assertEquals("b", pair.getSentencePattern().pattern());
  }
}
