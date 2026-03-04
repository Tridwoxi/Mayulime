package unit;

import domain.old_codec.Parser;
import domain.old_codec.Parser.BadMapCodeException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class Codec {

    private static final List<String> VALID = List.of(
        "3.3.10.Many features, all supported...:,s1.1,r1.,r3.2,c1.1,f1.",
        "3.2.10.Checkpoints in a funny order...:,s1.,c7.,f1.,c14.,c5.,c11.",
        "3.3.0.Partial fill...:,s1.,f1."
    );

    private static final List<String> INVALID = List.of(
        "3.1.10.Multiple starts...:,s1.,s1.,f1.",
        "3.1.9.Missing finish...:,s1.",
        "3.2.9.Unsupported teleport (for now)...:,s1.,t1.,f1.1,u1.", // Move when supported.
        "1000000.1000000.1000000.DOS attack...:,s1.,f1.",
        "3.2.10.Out of bounds...:,s1.1,r1.,r3.2,c1.1,f1.",
        "3.3.10.Spurious comma...:,s1.1,r1.,r3,.2,c1.1,f1.",
        "3.3.10.Spurious period...:s1.1,r1.,r3..2,c1.1,f1."
    );

    @Test
    public void truePositives() throws BadMapCodeException {
        VALID.forEach(mapCode ->
            Assertions.assertDoesNotThrow(() -> Parser.parse(mapCode), mapCode)
        );
    }

    @Test
    public void trueNegatives() throws BadMapCodeException {
        INVALID.forEach(mapCode ->
            Assertions.assertThrows(BadMapCodeException.class, () -> Parser.parse(mapCode), mapCode)
        );
    }
}
