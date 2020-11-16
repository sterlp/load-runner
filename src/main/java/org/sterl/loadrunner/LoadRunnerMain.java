package org.sterl.loadrunner;

import org.sterl.loadrunner.cmd.CommandLineLoadTest;

public class LoadRunnerMain {

    
    public static void main(String[] args) throws Exception {
        new CommandLineLoadTest().run(args);
    }
}
