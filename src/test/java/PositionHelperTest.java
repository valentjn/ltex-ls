import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PositionHelperTest {

    private final int[] lineStartPositions = PositionHelper.getLineStartPositions("Hello\n" +
            "Enthusiastic\r\n" +
            "Reader!");

    @Test
    public void testGetPosition_first_character(){
        Position position = PositionHelper.getPosition(0, lineStartPositions);

        Assertions.assertEquals(new Position(0, 0),position);
    }

    @Test
    public void testGetPosition_starts_with_newline(){
        Position position = PositionHelper.getPosition(1, lineStartPositions);

        Assertions.assertEquals(new Position(1, 0),position);
    }

    @Test
    public void testGetPosition_second_start(){
        Position position = PositionHelper.getPosition(6, lineStartPositions);

        Assertions.assertEquals(new Position(1, 0),position);
    }

    @Test
    public void testGetPosition_second_line_second_character(){
        Position position = PositionHelper.getPosition(7, lineStartPositions);

        Assertions.assertEquals(new Position(1, 1),position);
    }

    @Test
    public void testGetPosition(){
        Position position = PositionHelper.getPosition(12, lineStartPositions);

        Assertions.assertEquals(new Position(1, 6),position);
    }

    @Test
    public void testGetStartPositions(){
        int[] startPositions = lineStartPositions;

        Assertions.assertArrayEquals(new int[]{0, 6, 20}, startPositions);
    }
}