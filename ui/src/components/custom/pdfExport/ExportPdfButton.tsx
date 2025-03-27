import React from 'react';
import { IconButton, Tooltip } from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import { ConfigType } from '../multiMatrixProvider';
import { generateMatrixPdf } from './MatrixPdfExporter';

interface ExportPdfButtonProps {
  config: ConfigType;
  items: Array<{
    itemName: string;
    criticality: number;
    accessibility: number;
    recoverability: number;
    vulnerability: number;
    effect: number;
    recognizability: number;
  }>;
}

export const ExportPdfButton: React.FC<ExportPdfButtonProps> = ({ config, items }) => {
  const handleExportPdf = () => {
    const matrixMap = new Map<string, Map<string, number>>();
    items.forEach((item) => {
      matrixMap.set(
        item.itemName,
        new Map([
          ["Criticality", item.criticality],
          ["Accessibility", item.accessibility],
          ["Recuperability", item.recoverability],
          ["Vulnerability", item.vulnerability],
          ["Effect", item.effect],
          ["Recognizability", item.recognizability],
        ])
      );
    });

    generateMatrixPdf({ config, matrixMap });
  };

  return (
    <Tooltip title="Export PDF">
      <IconButton
        onClick={handleExportPdf}
        sx={{
          color: '#9C27B0',
          '&:hover': {
            backgroundColor: 'rgba(156, 39, 176, 0.1)',
          },
        }}
      >
        <PictureAsPdfIcon />
      </IconButton>
    </Tooltip>
  );
}; 