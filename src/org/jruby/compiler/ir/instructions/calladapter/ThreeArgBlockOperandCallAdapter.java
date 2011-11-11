package org.jruby.compiler.ir.instructions.calladapter;

import org.jruby.compiler.ir.operands.Operand;
import org.jruby.interpreter.InterpreterContext;
import org.jruby.runtime.Block;
import org.jruby.runtime.CallSite;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 */
public class ThreeArgBlockOperandCallAdapter extends ClosureCallAdapter {
    private Operand arg1;
    private Operand arg2;
    private Operand arg3;
    
    public ThreeArgBlockOperandCallAdapter(CallSite callSite, Operand[] args, Operand closure) {
        super(callSite, closure);
        
        assert args.length == 3;
        
        arg1 = args[0];
        arg2 = args[1];
        arg3 = args[2];
    }

    @Override
    public Object call(InterpreterContext interp, ThreadContext context, IRubyObject self, IRubyObject receiver) {
        IRubyObject value1 = (IRubyObject) arg1.retrieve(interp, context, self);
        IRubyObject value2 = (IRubyObject) arg2.retrieve(interp, context, self);
        IRubyObject value3 = (IRubyObject) arg3.retrieve(interp, context, self);
        Block block = prepareBlock(interp, context, self);
        
        try {
            return callSite.call(context, self, receiver, value1, value2, value3, block);
        } finally {
            block.escape();
        }
    }
}