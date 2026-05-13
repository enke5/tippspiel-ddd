{{/*
Expand the name of the chart.
*/}}
{{- define "tippspiel.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "tippspiel.fullname" -}}
{{- printf "%s" (include "tippspiel.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "tippspiel.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Selector labels for a given component
Usage: include "tippspiel.selectorLabels" (dict "component" "tournament" "context" .)
*/}}
{{- define "tippspiel.selectorLabels" -}}
app.kubernetes.io/name: {{ include "tippspiel.name" .context }}
app.kubernetes.io/component: {{ .component }}
{{- end }}
