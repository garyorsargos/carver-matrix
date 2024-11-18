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
          <CategoryGroup category="Criticality" />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Accessibility" />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Recuperability" />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Vulnerability" />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Effect" />
        </Grid>
        <Grid item xs={6}>
          <CategoryGroup category="Recognizability" />
        </Grid>
      </Grid>
      </Box>
    );
  }
}

export default TargetGroup;
