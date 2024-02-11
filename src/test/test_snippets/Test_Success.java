package test_snippets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_Success {

    //A test that is expected to succeed.
    @Test
    private void test_success(){
        assertEquals(1,1);
    }
}
