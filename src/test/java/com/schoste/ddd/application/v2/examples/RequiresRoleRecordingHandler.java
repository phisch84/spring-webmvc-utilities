package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.application.v2.annotations.RecordingPreHandler;

/**
 * Second pre handler of {@link RequiresRole}, with a higher priority number than {@link RequiresRoleHandler}: it records
 * whether the interceptor went on after the first handler (not part of the README's example)
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class RequiresRoleRecordingHandler extends RecordingPreHandler<RequiresRole>
{
}
