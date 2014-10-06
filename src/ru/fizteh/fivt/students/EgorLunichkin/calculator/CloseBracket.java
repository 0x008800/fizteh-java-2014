import java.util.Stack;

public class CloseBracket extends Operator {
    public byte priority()
    {
        return -1;
    }

    public void operate(Stack<Operand> nums) throws CalculatorException
    {
        throw new CalculatorException("Operation with closing bracket");
    }

    public void pushElement (Stack<Operand> nums, Stack<Operator> ops) throws CalculatorException
    {
        try
        {
            while (ops.peek().priority() >= 0)
            {
                Operator cur_op = ops.pop();
                cur_op.operate(nums);
            }
            ops.pop();
        }
        catch (Exception e)
        {
            throw new CalculatorException(e.getMessage());
        }
    }
}
