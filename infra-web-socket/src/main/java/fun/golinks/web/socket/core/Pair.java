package fun.golinks.web.socket.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<T1, T2> {

    private T1 o1;
    private T2 o2;
}
