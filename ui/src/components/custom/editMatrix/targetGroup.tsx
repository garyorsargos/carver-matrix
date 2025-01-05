import { Box, Typography, Grid } from "@mui/material";
import { Component } from "react";
import CategoryGroup from "./categoryGroup";


interface TargetGroupProps {
  targetName: string;
}

export class TargetGroup extends Component<TargetGroupProps> {
  render() {
    const { targetName } = this.props;
    return (
      <Box>
        <Typography variant='h4'>{targetName}</Typography>
        <Grid container spacing={2}>
        <Grid item xs={6}>
          <CategoryGroup category="Criticality" targetTitle={targetName} />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Accessibility" targetTitle={targetName} /> 
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Recuperability" targetTitle={targetName} />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Vulnerability" targetTitle={targetName} />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Effect" targetTitle={targetName} />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Recognizability" targetTitle={targetName} />
        </Grid>
      </Grid>
      </Box>
    );
  }
}

export default TargetGroup;
