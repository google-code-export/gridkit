package azul.test.output;

public class PoisonTask implements Task {
	@Override
	public boolean isPoison() {
		return true;
	}
}
