**Hilbert** is a java simulator of a social system evolving over time based on probabilistic principles .

To run **Hilbert** you need the Java 11 runtime environment installed.

The `org.mmarini.hilber.app.Simulate` is the entry point of simualtion.
The command arguments are

```
usage: org.mmarini.hilbert.apps.Simulate
       [-h] [-v] [-r RULES] [-s STATUS] [-k KPIS] [-o OUTPUT]
       [-n NUMBER]

Run a session of simulation.

named arguments:
  -h, --help             show this help message and exit
  -v, --version          show current version
  -r RULES, --rules RULES
                         specify rules yaml file (default: rules.yml)
  -s STATUS, --status STATUS
                         specify status yaml file (default: status.yml)
  -k KPIS, --kpis KPIS   specify kpis csv file
  -o OUTPUT, --output OUTPUT
                         specify output yaml file (default: output.yml)
  -n NUMBER, --number NUMBER
                         specify   the   maximum   number   of   iterations
                         (default: 10000)

```

## Octave

The `octave` folder contain octave script to analyze the results.

`main.m` draws a chart with the main kpis.
