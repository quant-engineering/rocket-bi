<template>
  <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <defs>
      <linearGradient x1="7.47%" y1="72.151%" x2="86.28%" y2="31.104%" id="b">
        <stop stop-color="#59AFFF" offset="0%" />
        <stop stop-color="#6495FF" offset="100%" />
      </linearGradient>
      <path d="M3.5 0A1.5 1.5 0 0 0 2 1.5V12h20V1.5A1.5 1.5 0 0 0 20.5 0h-17z" id="a" />
    </defs>
    <g fill="none" fill-rule="evenodd">
      <path
        d="M22.5 4.5h-9.9L8.55.45C8.25.15 7.95 0 7.5 0h-6C.6 0 0 .6 0 1.5v21c0 .9.6 1.5 1.5 1.5h21c.9 0 1.5-.6 1.5-1.5V6c0-.9-.6-1.5-1.5-1.5z"
        fill="#AED4FF"
        fill-rule="nonzero"
      />
      <g transform="translate(0 1.5)">
        <use fill="#202F3E" xlink:href="#a" />
        <use fill="#FFF2BE" xlink:href="#a" />
      </g>
      <path
        d="M23 10.5H10.535L8.832 7.945A1 1 0 0 0 8 7.5H1a1 1 0 0 0-1 1v13a1 1 0 0 0 1 1h22a1 1 0 0 0 1-1v-10a1 1 0 0 0-1-1z"
        fill="url(#b)"
        fill-rule="nonzero"
        transform="translate(0 1.5)"
      />
      <path d="M3 15h7.5v3H3z" fill="#AED4FF" fill-rule="nonzero" />
    </g>
  </svg>
</template>

<script lang="ts">
import Component from 'vue-class-component';
import Vue from 'vue';

@Component
export default class DirectoryIcon extends Vue {}
</script>
