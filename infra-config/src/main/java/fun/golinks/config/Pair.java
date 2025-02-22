package fun.golinks.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<T1, T2> {

    private T1 o1;
    private T2 o2;
}
