import React from 'react';
import { IconButton, Tooltip } from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import { ConfigType } from '../multiMatrixProvider';
import { generateMatrixPdf } from './MatrixPdfExporter';

interface ExportPdfButtonProps {
  config: ConfigType;
  items: MatrixItem[];
}

interface ItemScore {
  [key: string]: number | undefined;
}

interface MatrixItem {
  itemName: string;
  criticality: ItemScore;
  accessibility: ItemScore;
  recoverability: ItemScore;
  vulnerability: ItemScore;
  effect: ItemScore;
  recognizability: ItemScore;
}

export const ExportPdfButton: React.FC<ExportPdfButtonProps> = ({ config, items }) => {
  const handleExportPdf = () => {
    const matrixMap = new Map<string, Map<string, number>>();
    const categoryToMultiplierMap: { [key: string]: keyof ConfigType } = {
      Criticality: "cMulti",
      Accessibility: "aMulti",
      Recuperability: "rMulti",
      Vulnerability: "vMulti",
      Effect: "eMulti",
      Recognizability: "r2Multi",
    };

    items.forEach((item: MatrixItem) => {
      const itemScores = new Map<string, number>();
      Object.entries(categoryToMultiplierMap).forEach(([category, multiplierKey]) => {
        // Get all non-empty scores for this category
        const categoryKey = category.toLowerCase();
        
        // Get the scores using the correct property name
        let categoryScores;
        if (categoryKey === 'recuperability') {
          categoryScores = item.recoverability;
          console.log('Recuperability scores:', categoryScores); // Debug log
          console.log('Multiplier:', config[multiplierKey]); // Debug log
        } else if (categoryKey === 'criticality') {
          categoryScores = item.criticality;
        } else if (categoryKey === 'accessibility') {
          categoryScores = item.accessibility;
        } else if (categoryKey === 'vulnerability') {
          categoryScores = item.vulnerability;
        } else if (categoryKey === 'effect') {
          categoryScores = item.effect;
        } else if (categoryKey === 'recognizability') {
          categoryScores = item.recognizability;
        }
        
        categoryScores = categoryScores || {};
        
        const scores = Object.values(categoryScores).filter((score): score is number => 
          score !== undefined && score !== null
        );
        
        // Calculate average if there are scores, otherwise use 0
        const average = scores.length > 0 
          ? scores.reduce((sum, score) => sum + score, 0) / scores.length 
          : 0;
        
        // Apply multiplier
        const multiplier = Number(config[multiplierKey]) || 1;
        const finalScore = average * multiplier;
        
        console.log(`Category ${category}:`, { scores, average, multiplier, finalScore }); // Debug log
        
        // Set the score in the map using the original category name to maintain case
        itemScores.set(category, finalScore);
      });
      matrixMap.set(item.itemName, itemScores);
    });

    console.log('Final matrixMap:', Object.fromEntries(matrixMap)); // Debug log
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