export interface ReportEvidence {
  imageUrls: string[];
  observation: string | null;
}

export interface BackendReporte {
  reporteId: number;
  titulo: string;
  descripcion: string | null;
  evidencia: ReportEvidence | null;
  fechaCreado: string;
  tipoReporte: string;
  estado: boolean;
}

export interface ReporteResumen {
  id: number;
  title: string;
  description: string | null;
  evidence: ReportEvidence | null;
  createdAt: Date;
  type: string;
  status: boolean;
}
