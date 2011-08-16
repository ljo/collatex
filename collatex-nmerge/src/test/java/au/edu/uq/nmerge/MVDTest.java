/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.edu.uq.nmerge;

import au.edu.uq.nmerge.mvd.Chunk;
import au.edu.uq.nmerge.mvd.ChunkState;
import au.edu.uq.nmerge.mvd.Collation;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MVDTest extends AbstractTest {

  public static final byte[] VERSION_2 = "Hello World! How funny!".getBytes();
  public static final byte[] VERSION_1 = "Hello funny World!".getBytes();

  @Test
  public void simple() throws Exception {
    final Collation collation = new Collation("Test");
    final short version1 = (short) collation.newVersion("test1", "test1");
    collation.update(version1, VERSION_1);

    final short version2 = (short) collation.newVersion("test2", "test2");
    collation.update(version2, VERSION_2);

    for (Chunk c : collation.compare(version2, version1, ChunkState.ADDED)) {
      LOG.debug(c.toString());
    }
  }
}