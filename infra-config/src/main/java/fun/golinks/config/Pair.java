package fun.golinks.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Pair<T1, T2> {

    @Getter
    private T1 o1;
    @Getter
    private T2 o2;
}
