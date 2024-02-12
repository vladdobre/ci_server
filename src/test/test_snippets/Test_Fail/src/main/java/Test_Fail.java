package test_snippets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_Fail {


    //A test that is expected to fail
    @Test
    public void test_fail(){
        assertEquals(1,5);
    }
}
