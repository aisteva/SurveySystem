package Controllers.Implementations;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by paulinaoveraite on 2017-05-23.
 */
@Getter
@Setter
public class ScaleLimits {

    private int max;
    private int min;

    public ScaleLimits(int min, int max) {
        this.min = min;
        this.max = max;
    }
}
