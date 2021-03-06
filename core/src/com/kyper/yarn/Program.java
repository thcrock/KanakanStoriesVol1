package com.kyper.yarn;


import com.kyper.yarn.Lexer.Token;
import com.kyper.yarn.Lexer.TokenType;
import com.kyper.yarn.Library.FunctionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Program {

	protected HashMap<String, String> strings = new HashMap<String, String>();
	protected HashMap<String, LineInfo> line_info = new HashMap<String, Program.LineInfo>();

	protected HashMap<String, Node> nodes = new HashMap<String, Node>();

	private int string_count = 0;

	public Map<String, Node> getNodes() {
		return nodes;
	}

	/// Loads a new string table into the program.
	/**
	 * The string table is merged with any existing strings, with the new table
	 * taking precedence over the old.
	 */
	public void loadStrings(Map<String, String> new_strings) {
		for (Map.Entry<String, String> line : new_strings.entrySet()) {
			strings.put(line.getKey(), line.getValue());
		}
	}

	public String registerString(String string, String node_name, String line_id, int line_number,
			boolean localisable) {
		String key;

		if (line_id == null)
			key = StringUtils.format("%1$s - %2$s", node_name, string_count++);
		else
			key = line_id;

		//its not int he list; append it
		strings.put(key, string);

		if (localisable) {
			//additionally, keep info about this string around
			line_info.put(key, new LineInfo(node_name, line_number));
		}

		return key;
	}

	public String getString(String key) {
		String value = null;
		if (strings.containsKey(key))
			value = strings.get(key);
		return value;
	}

	public String dumpCode(Library lib) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, Node> entry : nodes.entrySet()) {
			sb.append("Node \n" + entry.getKey() + ":");
			int instruction_count = 0;

			ArrayList<Instruction> instructions = entry.getValue().instructions;
			for (int i = 0; i < instructions.size(); i++) {
				Instruction instruction = instructions.get(i);
				String instruction_text = null;

				if (instruction.getOperation() == ByteCode.Label) {
					instruction_text = instruction.toString(this, lib);
				} else {
					instruction_text = "    " + instruction.toString(this, lib);
				}

				String preface;
				if (instruction_count % 5 == 0 || instruction_count == entry.getValue().instructions.size() - 1) {
					preface = StringUtils.format("%1$6s", instruction_count + "");
				} else {
					preface = StringUtils.format("%1$6s    ", " ");
				}

				sb.append(preface + instruction_text + "\n");
				instruction_count++;
			}
			sb.append("\n\n");
		}

		for (Map.Entry<String, String> entry : strings.entrySet()) {
			LineInfo line_info = this.line_info.get(entry.getKey());
			if(line_info == null)
				continue;
			sb.append(StringUtils.format("%1$s: %2$s  (%3$s:%4$s)\n", entry.getKey(), entry.getValue(), line_info.getNodeName(),
					line_info.getLineNumber()));
		}

		return sb.toString();
	}

	public String getTextForNode(String node_name) {
		String key = nodes.get(node_name).source_string_id;
		return this.getString(key == null ? "" : key);
	}

	public void include(Program other_program) {
		for (Map.Entry<String, Node> other : other_program.nodes.entrySet()) {
			if (nodes.containsKey(other.getKey())) {
				throw new IllegalStateException(
						StringUtils.format("This program already contains a node named %s", other.getKey()));
			}

			nodes.put(other.getKey(), other.getValue());
		}

		for (Map.Entry<String, String> other : other_program.strings.entrySet()) {
			//TODO: this seems fishy -- maybe check strings map instead?
			if (nodes.containsKey(other.getKey())) {
				throw new IllegalStateException(
						StringUtils.format("This program already contains a string with key %s", other.getKey()));
			}
			strings.put(other.getKey(), other.getValue());
		}
	}

	// When saving programs, we want to save only lines that do NOT have a line: key.
	// This is because these lines will be loaded from a string table.
	// However, because certain strings (like those used in expressions) won't have tags,
	// they won't be included in generated string tables, so we need to export them here.

	// We do this by NOT including the main strings list, and providing a property
	// that gets serialised as "strings" in the output, which includes all untagged strings.
	protected HashMap<String, String> untaggedStrings() {
		HashMap<String, String> result = new HashMap<String, String>();

		for (Map.Entry<String, String> line : strings.entrySet()) {
			if (line.getKey().startsWith("line:"))//TODO ============maybe change to Line since thats what the parser spits out
				continue;
			result.put(line.getKey(), line.getValue());
		}
		return result;
	}

	protected static class ParseException extends RuntimeException {
		private static final long serialVersionUID = -6422941521497633431L;

		protected int line_number = 0;

		public ParseException(String message) {
			super(message);
		}

		public ParseException(String message, Exception cause) {
			super(message, cause);
		}

		protected static ParseException make(Token found_token, TokenType... expected_types) {
			int line_number = found_token.line_number + 1;

			ArrayList<String> expected_type_names = new ArrayList<String>();
			for (TokenType type : expected_types) {
				expected_type_names.add(type.name());
			}
			String possible_values = String.join(",", expected_type_names);
			String message = StringUtils.format("Line %1$s:%2$s: Expected %3$s, but found %4$s", line_number,
					found_token.column_number, possible_values, found_token.type.name());
			ParseException e = new ParseException(message);
			e.line_number = line_number;
			return e;
		}

		protected static ParseException make(Token most_recent_token, String message) {
			int line_number = most_recent_token.line_number + 1;
			String m = StringUtils.format("Line %1$s:%2$s: %3$s", line_number, most_recent_token.column_number, message);
			ParseException e = new ParseException(m);
			e.line_number = line_number;
			return e;
		}
	}

	protected static class LineInfo {
		private int line_number;
		private String node_name;

		public LineInfo(String node_name, int line_number) {
			this.node_name = node_name;
			this.line_number = line_number;
		}

		public int getLineNumber() {
			return line_number;
		}

		public String getNodeName() {
			return node_name==null?"null":node_name;
		}
	}

	protected static class Node {

		public ArrayList<Instruction> instructions = new ArrayList<Program.Instruction>();
		public String name;

		//the entry in the programs string table that contains
		//the original text of this node. null if not available
		public String source_string_id = null;

		public HashMap<String, Integer> labels = new HashMap<String, Integer>();

		public ArrayList<String> tags;

	}

	protected static enum ByteCode {
		/// opA = string: label name
		Label,
		/// opA = string: label name
		JumpTo,
		/// peek string from stack and jump to that label
		Jump,
		/// opA = int: string number
		RunLine,
		/// opA = string: command text
		RunCommand,
		/// opA = int: string number for option to add
		AddOption,
		/// present the current list of options, then clear the list; most recently selected option will be on the top of the stack
		ShowOptions,
		/// opA = int: string number in table; push string to stack
		PushString,
		/// opA = float: number to push to stack
		PushNumber,
		/// opA = int (0 or 1): bool to push to stack
		PushBool,
		/// pushes a null value onto the stack
		PushNull,
		/// opA = string: label name if top of stack is not null, zero or false, jumps to that label
		JumpIfFalse,
		/// discard top of stack
		Pop,
		/// opA = string; looks up function, pops as many arguments as needed, result is pushed to stack
		CallFunc,
		/// opA = name of variable to get value of and push to stack
		PushVariable,
		/// opA = name of variable to store top of stack in
		StoreVariable,
		/// stops execution
		Stop,
		/// run the node whose name is at the top of the stack
		RunNode
	}

	protected static class Instruction {
		private ByteCode operation;
		private Object operandA;
		private Object operantB;

		public Instruction() {
		}

		public Instruction(ByteCode operation, Object operandA, Object operandB) {
			this.operandA = operandA;
			this.operantB = operandB;
			this.operation = operation;
		}

		public ByteCode getOperation() {
			return operation;
		}

		public void setOperation(ByteCode operation) {
			this.operation = operation;
		}

		public Object operandA() {
			return operandA;
		}

		public Object operandB() {
			return operantB;
		}

		public void setOperandA(Object operandA) {
			this.operandA = operandA;
		}

		public void setOperandB(Object operandB) {
			this.operantB = operandB;
		}

		public String toString(Program p, Library l) {
			// Labels are easy: just dump out the name
			if (operation == ByteCode.Label) {
				return operandA + ":";
			}

			// Convert the operands to strings
			String opAString = operandA != null ? operandA.toString() : "";
			String opBString = operantB != null ? operantB.toString() : "";

			// Generate a comment, if the instruction warrants it
			String comment = "";

			// Stack manipulation comments
			int pops = 0;
			int pushes = 0;

			switch (operation) {

			// These operations all push a single value to the stack
			case PushBool:
			case PushNull:
			case PushNumber:
			case PushString:
			case PushVariable:
			case ShowOptions:
				pushes = 1;
				break;

			// Functions pop 0 or more values, and pop 0 or 1
			case CallFunc:
				FunctionInfo function = l.getFunction((String) operandA);

				pops = function.getParamCount();

				if (function.returnsValue())
					pushes = 1;

				break;

			// Pop always pops a single value
			case Pop:
				pops = 1;
				break;

			// Switching to a different node will always clear the stack
			case RunNode:
				comment += "Clears stack";
				break;
			default:
				break;
			}

			// If we had any pushes or pops, report them

			if (pops > 0 && pushes > 0)
				comment += StringUtils.format("Pops %1$s, Pushes %2$s", pops, pushes);
			else if (pops > 0)
				comment += StringUtils.format("Pops %s", pops);
			else if (pushes > 0)
				comment += StringUtils.format("Pushes %s", pushes);

			// String lookup comments
			switch (operation) {
			case PushString:
			case RunLine:
			case AddOption:

				// Add the string for this option, if it has one
				if ((String) operandA != null) {
					String text = p.getString((String) operandA);
					comment += StringUtils.format("\"%s\"", text);
				}

				break;
			default:
				break;

			}

			if (comment != "") {
				comment = "; " + comment;
			}

			return StringUtils.format("%1$-15s %2$-10s %3$-10s %4$-10s", operation.name(), opAString, opBString, comment);
		}
	}

}
