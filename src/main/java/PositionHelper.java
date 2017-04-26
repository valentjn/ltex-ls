import org.eclipse.lsp4j.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class PositionHelper {

    @NotNull
    public static Position getPosition(int pos, int[] lineStarts){

        int line = Arrays.binarySearch(lineStarts, pos);

        if (line < 0){
            int insertion_point = -1 * line - 1;
            line = insertion_point - 1;
        }

        return new Position(line, pos - lineStarts[line]);
    }


    public static int[] getLineStartPositions(String text) {
        ArrayList<Integer> lineStartPositions = new ArrayList<>();
        lineStartPositions.add(0);
        for (int i = 0; i < text.length(); i++){
            char c = text.charAt(i);

            if (c == '\r'){
                if (i+1 < text.length() && text.charAt(i+1) == '\n'){
                    i++;
                }

                lineStartPositions.add(i+1);
            }
            else if (c == '\n') {
                lineStartPositions.add(i+1);
            }
        }

        return lineStartPositions.stream().mapToInt(i -> i).toArray();
    }
}
